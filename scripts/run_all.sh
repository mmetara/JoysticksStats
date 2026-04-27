#!/usr/bin/env sh
set -eu

PROJECT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$PROJECT_DIR"

OS_NAME="$(uname -s)"
HOST_ARCH="$(uname -m)"

if [ "$HOST_ARCH" = "arm64" ] || [ "$HOST_ARCH" = "aarch64" ]; then
  PREFERRED_ABI="arm64-v8a"
  SECONDARY_ABI="x86_64"
else
  PREFERRED_ABI="x86_64"
  SECONDARY_ABI="arm64-v8a"
fi

choose_system_image() {
  if [ -z "${SDKMANAGER_PATH:-}" ]; then
    echo "system-images;android-35;google_apis;$PREFERRED_ABI"
    return
  fi

  SDK_LIST="$($SDKMANAGER_PATH --sdk_root="$ANDROID_SDK_ROOT" --list 2>/dev/null || true)"

  for CANDIDATE in \
    "system-images;android-35;google_apis;$PREFERRED_ABI" \
    "system-images;android-35;google_apis;$SECONDARY_ABI" \
    "system-images;android-34;google_apis;$PREFERRED_ABI" \
    "system-images;android-34;google_apis;$SECONDARY_ABI"; do
    if printf '%s\n' "$SDK_LIST" | grep -q "$CANDIDATE"; then
      echo "$CANDIDATE"
      return
    fi
  done

  echo "system-images;android-35;google_apis;$PREFERRED_ABI"
}

ensure_brew_in_path() {
  if command -v brew >/dev/null 2>&1; then
    return
  fi

  if [ -x "/opt/homebrew/bin/brew" ]; then
    export PATH="/opt/homebrew/bin:$PATH"
  elif [ -x "/usr/local/bin/brew" ]; then
    export PATH="/usr/local/bin:$PATH"
  fi
}

ensure_java() {
  if java -version >/dev/null 2>&1; then
    return
  fi

  if [ "$OS_NAME" != "Darwin" ]; then
    echo "Java runtime missing. Install JDK 17+, then retry ./run_all.sh."
    exit 1
  fi

  ensure_brew_in_path

  if ! command -v brew >/dev/null 2>&1; then
    echo "Java missing and Homebrew not found. Run ./install.sh first."
    exit 1
  fi

  if ! brew list --versions openjdk@17 >/dev/null 2>&1; then
    echo "Installing openjdk@17 with Homebrew..."
    brew install openjdk@17
  fi

  JAVA_HOME_PATH="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home"
  if [ -d "$JAVA_HOME_PATH" ]; then
    export JAVA_HOME="$JAVA_HOME_PATH"
    export PATH="$JAVA_HOME/bin:$PATH"
  fi

  if ! java -version >/dev/null 2>&1; then
    echo "Java is still unavailable after setup."
    echo "Run ./install.sh, then restart terminal, then retry ./run_all.sh."
    exit 1
  fi
}

SDKMANAGER_PATH=""
AVDMANAGER_PATH=""
EMULATOR_PATH=""

ensure_sdkmanager() {
  if command -v sdkmanager >/dev/null 2>&1; then
    SDKMANAGER_PATH="$(command -v sdkmanager)"
    return
  fi

  if [ -x "/opt/homebrew/share/android-commandlinetools/cmdline-tools/latest/bin/sdkmanager" ]; then
    SDKMANAGER_PATH="/opt/homebrew/share/android-commandlinetools/cmdline-tools/latest/bin/sdkmanager"
    return
  fi

  if [ -x "/usr/local/share/android-commandlinetools/cmdline-tools/latest/bin/sdkmanager" ]; then
    SDKMANAGER_PATH="/usr/local/share/android-commandlinetools/cmdline-tools/latest/bin/sdkmanager"
    return
  fi

  if [ "$OS_NAME" != "Darwin" ]; then
    return
  fi

  ensure_brew_in_path

  if ! command -v brew >/dev/null 2>&1; then
    return
  fi

  if ! brew list --cask android-commandlinetools >/dev/null 2>&1; then
    echo "Installing android-commandlinetools with Homebrew..."
    brew install --cask android-commandlinetools
  fi

  if [ -x "/opt/homebrew/share/android-commandlinetools/cmdline-tools/latest/bin/sdkmanager" ]; then
    SDKMANAGER_PATH="/opt/homebrew/share/android-commandlinetools/cmdline-tools/latest/bin/sdkmanager"
    return
  fi

  if [ -x "/usr/local/share/android-commandlinetools/cmdline-tools/latest/bin/sdkmanager" ]; then
    SDKMANAGER_PATH="/usr/local/share/android-commandlinetools/cmdline-tools/latest/bin/sdkmanager"
    return
  fi
}

ensure_avdmanager() {
  if [ -n "$ANDROID_SDK_ROOT" ] && [ -x "$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/avdmanager" ]; then
    AVDMANAGER_PATH="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/avdmanager"
    return
  fi

  if command -v avdmanager >/dev/null 2>&1; then
    AVDMANAGER_PATH="$(command -v avdmanager)"
    return
  fi

  if [ -x "/opt/homebrew/share/android-commandlinetools/cmdline-tools/latest/bin/avdmanager" ]; then
    AVDMANAGER_PATH="/opt/homebrew/share/android-commandlinetools/cmdline-tools/latest/bin/avdmanager"
    return
  fi

  if [ -x "/usr/local/share/android-commandlinetools/cmdline-tools/latest/bin/avdmanager" ]; then
    AVDMANAGER_PATH="/usr/local/share/android-commandlinetools/cmdline-tools/latest/bin/avdmanager"
    return
  fi

}

ensure_emulator_binary() {
  if command -v emulator >/dev/null 2>&1; then
    EMULATOR_PATH="$(command -v emulator)"
    return
  fi

  if [ -n "$ANDROID_SDK_ROOT" ] && [ -x "$ANDROID_SDK_ROOT/emulator/emulator" ]; then
    EMULATOR_PATH="$ANDROID_SDK_ROOT/emulator/emulator"
    return
  fi

  if [ -x "$HOME/Library/Android/sdk/emulator/emulator" ]; then
    EMULATOR_PATH="$HOME/Library/Android/sdk/emulator/emulator"
    return
  fi
}

ensure_android_sdk() {
  ANDROID_SDK_ROOT_DEFAULT="$HOME/Library/Android/sdk"
  ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$ANDROID_SDK_ROOT_DEFAULT}"
  export ANDROID_SDK_ROOT
  mkdir -p "$ANDROID_SDK_ROOT"

  if [ ! -f "./local.properties" ]; then
    cat > ./local.properties <<EOF
sdk.dir=$ANDROID_SDK_ROOT
EOF
    echo "Created local.properties with sdk.dir=$ANDROID_SDK_ROOT"
  fi

  if [ -x "$ANDROID_SDK_ROOT/platform-tools/adb" ]; then
    export PATH="$ANDROID_SDK_ROOT/platform-tools:$PATH"
  fi

  ensure_sdkmanager

  if [ -z "$SDKMANAGER_PATH" ] && [ -x "$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" ]; then
    SDKMANAGER_PATH="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager"
  fi

  if [ -z "$SDKMANAGER_PATH" ]; then
    echo "sdkmanager not found."
    echo "Run ./install.sh first, then retry ./run_all.sh."
    exit 1
  fi

  echo "Accepting Android SDK licenses..."
  yes | "$SDKMANAGER_PATH" --sdk_root="$ANDROID_SDK_ROOT" --licenses >/dev/null || true

  SYSTEM_IMAGE_PACKAGE="$(choose_system_image)"

  echo "Installing required Android SDK components..."
  "$SDKMANAGER_PATH" --sdk_root="$ANDROID_SDK_ROOT" \
    "cmdline-tools;latest" \
    "platform-tools" \
    "platforms;android-36.1" \
    "build-tools;36.0.0" \
    "emulator" \
    "$SYSTEM_IMAGE_PACKAGE"

  ensure_avdmanager
  ensure_emulator_binary
}

device_count() {
  if command -v adb >/dev/null 2>&1; then
    adb devices | awk 'NR>1 && $2=="device" {count++} END {print count+0}'
  else
    echo 0
  fi
}

choose_target_serial() {
  if ! command -v adb >/dev/null 2>&1; then
    return
  fi

  EMU_SERIAL="$(adb devices | awk 'NR>1 && $2=="device" && $1 ~ /^emulator-/ {print $1; exit}')"
  if [ -n "$EMU_SERIAL" ]; then
    echo "$EMU_SERIAL"
    return
  fi

  adb devices | awk 'NR>1 && $2=="device" {print $1; exit}'
}

ensure_emulator_running() {
  if [ "$(device_count)" -gt 0 ]; then
    return
  fi

  if [ -z "$AVDMANAGER_PATH" ] || [ -z "$EMULATOR_PATH" ]; then
    echo "avdmanager or emulator binary not found."
    echo "Re-run ./install.sh then ./run_all.sh."
    exit 1
  fi

  AVD_NAME="joysticks_api35_${PREFERRED_ABI}"
  SYSTEM_IMAGE="$(choose_system_image)"

  if ! "$EMULATOR_PATH" -list-avds | grep -qx "$AVD_NAME"; then
    echo "Creating emulator profile: $AVD_NAME"
    mkdir -p "$HOME/.android/avd"
    echo "no" | ANDROID_SDK_ROOT="$ANDROID_SDK_ROOT" ANDROID_HOME="$ANDROID_SDK_ROOT" "$AVDMANAGER_PATH" create avd -n "$AVD_NAME" -k "$SYSTEM_IMAGE" --force
  fi

  echo "Starting Android emulator: $AVD_NAME"
  nohup "$EMULATOR_PATH" -avd "$AVD_NAME" -no-boot-anim -no-snapshot-save >/tmp/joysticks-emulator.log 2>&1 &

  echo "Waiting for emulator to connect..."
  connected="0"
  connect_attempts=0
  while [ "$connected" = "0" ] && [ "$connect_attempts" -lt 120 ]; do
    if [ "$(device_count)" -gt 0 ]; then
      connected="1"
      break
    fi
    connect_attempts=$((connect_attempts + 1))
    sleep 2
  done

  if [ "$connected" != "1" ]; then
    echo "Emulator did not connect in time. See /tmp/joysticks-emulator.log"
    exit 1
  fi

  echo "Waiting for Android boot to complete..."
  boot_completed="0"
  attempts=0
  while [ "$boot_completed" != "1" ] && [ "$attempts" -lt 120 ]; do
    boot_completed="$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')"
    if [ "$boot_completed" = "1" ]; then
      break
    fi
    attempts=$((attempts + 1))
    sleep 2
  done

  if [ "$boot_completed" != "1" ]; then
    echo "Emulator did not boot in time. See /tmp/joysticks-emulator.log"
    exit 1
  fi

  adb shell input keyevent 82 >/dev/null 2>&1 || true
}

if [ ! -f "./gradlew" ]; then
  echo "gradlew not found. Run this script from the project root."
  exit 1
fi

chmod +x ./gradlew
ensure_java
ensure_android_sdk

echo "Preparing clean build state..."
./gradlew --stop >/dev/null 2>&1 || true
rm -rf ./app/build/intermediates/project_dex_archive ./app/build/intermediates/dex ./app/build/intermediates/dex_archive_input_jar_hashes

echo "Building debug APK (clean)..."
./gradlew clean :app:assembleDebug

ensure_emulator_running
DEVICE_COUNT=$(device_count)

if [ "$DEVICE_COUNT" -gt 0 ]; then
  TARGET_SERIAL="$(choose_target_serial)"
  if [ -n "$TARGET_SERIAL" ]; then
    export ANDROID_SERIAL="$TARGET_SERIAL"
    echo "Using target device: $ANDROID_SERIAL"
  fi

  echo "Installing debug APK on connected device/emulator..."
  ./gradlew :app:installDebug

  echo "Launching app..."
  adb shell am start -n com.joysticks.stats/.MainActivity || true
else
  echo "No Android device/emulator detected."
  echo "APK built successfully; start an emulator and run:"
  echo "  ./gradlew :app:installDebug"
fi

echo "Done."

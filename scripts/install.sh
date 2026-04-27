#!/usr/bin/env sh
set -eu

PROJECT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$PROJECT_DIR"

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

if [ "$(uname -s)" != "Darwin" ]; then
  echo "This installer targets macOS (Homebrew)."
  exit 1
fi

ensure_brew_in_path

if ! command -v brew >/dev/null 2>&1; then
  echo "Homebrew not found. Installing Homebrew..."
  /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
  ensure_brew_in_path
fi

echo "Updating Homebrew..."
brew update

echo "Installing required packages..."
brew install git openjdk@17 android-platform-tools
brew install --cask android-commandlinetools || true

echo "Installing Android Studio (if not already installed)..."
brew install --cask android-studio || true

JAVA_HOME_PATH="$(brew --prefix openjdk@17)/libexec/openjdk.jdk/Contents/Home"

if [ -d "$JAVA_HOME_PATH" ]; then
  export JAVA_HOME="$JAVA_HOME_PATH"
  export PATH="$JAVA_HOME/bin:$PATH"
fi

ZPROFILE_PATH="$HOME/.zprofile"
JAVA_BLOCK_START="# >>> joysticksstats java >>>"
JAVA_BLOCK_END="# <<< joysticksstats java <<<"

if [ -f "$ZPROFILE_PATH" ]; then
  if ! grep -q "$JAVA_BLOCK_START" "$ZPROFILE_PATH"; then
    {
      echo ""
      echo "$JAVA_BLOCK_START"
      echo "export JAVA_HOME=\"$JAVA_HOME_PATH\""
      echo "export PATH=\"\$JAVA_HOME/bin:\$PATH\""
      echo "$JAVA_BLOCK_END"
    } >> "$ZPROFILE_PATH"
  fi
else
  {
    echo "$JAVA_BLOCK_START"
    echo "export JAVA_HOME=\"$JAVA_HOME_PATH\""
    echo "export PATH=\"\$JAVA_HOME/bin:\$PATH\""
    echo "$JAVA_BLOCK_END"
  } > "$ZPROFILE_PATH"
fi

ANDROID_SDK_ROOT_DEFAULT="$HOME/Library/Android/sdk"
ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$ANDROID_SDK_ROOT_DEFAULT}"
mkdir -p "$ANDROID_SDK_ROOT"

# Keep local.properties aligned with this machine.
cat > "$PROJECT_DIR/local.properties" <<EOF
sdk.dir=$ANDROID_SDK_ROOT
EOF

SDKMANAGER=""
if command -v sdkmanager >/dev/null 2>&1; then
  SDKMANAGER="$(command -v sdkmanager)"
elif [ -x "/opt/homebrew/share/android-commandlinetools/cmdline-tools/latest/bin/sdkmanager" ]; then
  SDKMANAGER="/opt/homebrew/share/android-commandlinetools/cmdline-tools/latest/bin/sdkmanager"
elif [ -x "/usr/local/share/android-commandlinetools/cmdline-tools/latest/bin/sdkmanager" ]; then
  SDKMANAGER="/usr/local/share/android-commandlinetools/cmdline-tools/latest/bin/sdkmanager"
elif [ -x "$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager" ]; then
  SDKMANAGER="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager"
fi

if [ -n "$SDKMANAGER" ]; then
  echo "Accepting Android SDK licenses..."
  yes | "$SDKMANAGER" --sdk_root="$ANDROID_SDK_ROOT" --licenses >/dev/null || true

  echo "Installing Android SDK components (API 36)..."
  "$SDKMANAGER" --sdk_root="$ANDROID_SDK_ROOT" \
    "platform-tools" \
    "platforms;android-36.1" \
    "build-tools;36.0.0" || true
else
  echo "sdkmanager not found yet."
  echo "Install android-commandlinetools with Homebrew, then re-run this script."
fi

echo "Ensuring Gradle wrapper is executable..."
chmod +x "$PROJECT_DIR/gradlew"

echo "Environment check:"
java -version || true
adb version || true

cat <<'MSG'
Installation finished.

Recommended next steps:
1) Restart your terminal (or run: source ~/.zprofile).
2) Run: ./run_all.sh
MSG

# Guide execution locale - application Android Kotlin

## Objectif

Executer JoysticksStats localement avec le chemin recommande.

## Commandes recommandees

1. Provision environnement (macOS):
   - `./install.sh`
2. Build + emulateur + install + lancement:
   - `./run_all.sh`

## Notes

- `run_all.sh` prepare le build debug, demarre un emulateur si necessaire, installe l'APK et lance l'application.
- En diagnostic cible, utiliser `./gradlew clean :app:assembleDebug` puis `./gradlew :app:installDebug`.

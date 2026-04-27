# Base de connaissance - JoysticksStats

Utiliser cette arborescence comme index de navigation documentaire; la verite normative unique est portee par `docs/00-gouvernance/contrat-collaboration-ia.md`.

## Demarrage rapide agent IA

1. Lire `docs/00-gouvernance/contrat-collaboration-ia.md` en entier.
2. Lire `docs/00-gouvernance/table-matieres-structures.json`.
3. Lire `AGENTS.md` comme guide d'execution agent.
4. Lire `docs/01-vue-ensemble-solution/cartographie-solution.md`.
5. Ouvrir les standards du scope Android:
   - `docs/20-android-kotlin-technique/standards-application-android-kotlin.md`

## Structure

- `docs/00-gouvernance/`: contrat, checklist, table de routage.
- `docs/01-vue-ensemble-solution/`: cartographie globale.
- `docs/10-application/`: documentation fonctionnelle metier (quoi/pourquoi).
- `docs/20-android-kotlin-technique/`: documentation technique Android/Kotlin (comment).
- `docs/80-entree-projet/`: modele d'entree projet.

Regle anti-doublons: `docs/00-gouvernance/regle-anti-doublons.md`.

## Documentation applicative

- `docs/10-application/contexte-application.md`
- `docs/10-application/specification-fonctionnelle.md`
- `docs/10-application/architecture-specifique.md`

## Execution locale

1. Provision macOS: `./scripts/install.sh`
2. Build + emulateur + install + run: `./scripts/run_all.sh`

## Build manuel

- `./gradlew clean :app:assembleDebug`
- `./gradlew :app:installDebug`

# Cartographie solution - JoysticksStats

## Vue globale

JoysticksStats est une application Android Compose pour la conduite et la trace d'un match de baseball.

Composants principaux:

- App Android (`app/`)
- Moteur metier de match (`app/src/main/java/com/joysticks/stats/engine/`)
- Couche ecrans Compose (`app/src/main/java/com/joysticks/stats/ui/screens/`)
- Adaptateurs data et sorties (`data/`, `utils/`)
- Scripts execution locale (`scripts/install.sh`, `scripts/run_all.sh`)

## Decoupage operatoire par niveaux

- Niveau 0: build, run, environnement
- Niveau 1: entree et navigation
- Niveau 2: ecrans metier
- Niveau 3: composants transverses UI
- Niveau 4: coeur metier
- Niveau 5: adaptateurs donnees/sorties

Reference d'execution agent:

- `AGENTS.md`

## Chaines fonctionnelles canoniques

1. Import local alignement:
   `HomeScreen` -> `parseRoster` -> `TeamDataStore` -> `GameViewModel.loadRoster` -> `GameScreen`
2. Telechargement distant alignement:
   `HomeScreen` -> `AlignementRepository.downloadRoster` -> `parseRoster` -> `GameViewModel.loadRoster` -> `GameScreen`
3. Conduite du match:
   `GameScreen` + `BattingScreen` + `OpponentScoreScreen` -> `GameViewModel` -> `GameState`
4. Correction feuille de match:
   `StatsSheetScreen` -> route `game` ciblee -> `BattingScreen` -> `editAtBatEvent` -> `StatsSheetScreen`
5. Exports:
   CSV via `generateCsv`, PDF via `PdfExportUtils`.

## Frontieres techniques

- UI declenche des intentions.
- Le coeur metier porte les transitions d'etat.
- Les adaptateurs portent I/O (CSV, reseau, DataStore, PDF).

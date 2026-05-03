# AGENTS - JoysticksStats

Ce fichier definit le decoupage operatoire par niveaux pour les agents qui travaillent dans ce depot.

## 0. Ordre de lecture obligatoire

1. `docs/00-gouvernance/contrat-collaboration-ia.md`
2. `docs/00-gouvernance/table-matieres-structures.json`
3. `README.md`
4. `docs/01-vue-ensemble-solution/cartographie-solution.md`
5. `docs/20-android-kotlin-technique/standards-application-android-kotlin.md`
6. `docs/10-application/specification-fonctionnelle.md`

Regle d'application immediate:

- Avant toute reponse de process (socle, methode, workflow), effectuer cette lecture complete d'abord.
- La regle canonique de gouvernance est maintenue dans `docs/00-gouvernance/contrat-collaboration-ia.md`.

## 1. Niveaux d'architecture

### Niveau 0 - Plateforme

Role: assurer build, run, environnement.

Perimetre:

- settings.gradle.kts
- build.gradle.kts
- gradle/libs.versions.toml
- app/build.gradle.kts
- scripts/install.sh
- scripts/run_all.sh

Sorties attendues:

- projet compilable
- installation possible sur emulateur/device

### Niveau 1 - Entree et navigation

Role: lancer l'app et router les ecrans.

Perimetre:

- app/src/main/java/com/joysticks/stats/MainActivity.kt
- app/src/main/java/com/joysticks/stats/ui/navigation/AppNavigation.kt
- app/src/main/java/com/joysticks/stats/ui/navigation/Screen.kt

Sorties attendues:

- route correcte selon contexte
- callbacks d'export et de navigation operationnels

### Niveau 2 - UI metier

Role: interaction utilisateur.

Perimetre:

- app/src/main/java/com/joysticks/stats/ui/screens/SplashScreen.kt
- app/src/main/java/com/joysticks/stats/ui/screens/HomeScreen.kt
- app/src/main/java/com/joysticks/stats/ui/screens/GameScreen.kt
- app/src/main/java/com/joysticks/stats/ui/screens/BattingScreen.kt
- app/src/main/java/com/joysticks/stats/ui/screens/OpponentScoreScreen.kt
- app/src/main/java/com/joysticks/stats/ui/screens/StatsSheetScreen.kt
- app/src/main/java/com/joysticks/stats/ui/screens/TeamManagerScreen.kt
- app/src/main/java/com/joysticks/stats/ui/screens/LineUpScreen.kt

Sorties attendues:

- parcours utilisateur complet et coherent
- UX sans rupture entre ecrans

### Niveau 3 - Composants UI transverses

Role: mutualiser styles et composants.

Perimetre:

- app/src/main/java/com/joysticks/stats/ui/components/Hud.kt
- app/src/main/java/com/joysticks/stats/ui/components/Scoreboard.kt
- app/src/main/java/com/joysticks/stats/ui/theme/Theme.kt

Sorties attendues:

- apparence coherente
- composants reutilisables

### Niveau 4 - Coeur metier

Role: regler le match, tenir l'etat, appliquer les transitions.

Perimetre:

- app/src/main/java/com/joysticks/stats/engine/GameViewModel.kt
- app/src/main/java/com/joysticks/stats/engine/GameState.kt
- app/src/main/java/com/joysticks/stats/engine/Roster.kt
- app/src/main/java/com/joysticks/stats/engine/PlayerStats.kt

Sorties attendues:

- regles de baseball appliquees correctement
- etat immutable et deterministe

### Niveau 5 - Adaptateurs donnees et sorties

Role: interfacer CSV, stockage local, reseau, exports.

Perimetre:

- app/src/main/java/com/joysticks/stats/engine/RosterParser.kt
- app/src/main/java/com/joysticks/stats/engine/AlignementRepository.kt
- app/src/main/java/com/joysticks/stats/engine/CsvReader.kt
- app/src/main/java/com/joysticks/stats/data/Team.kt
- app/src/main/java/com/joysticks/stats/data/TeamDataStore.kt
- app/src/main/java/com/joysticks/stats/utils/TimeUtils.kt
- app/src/main/java/com/joysticks/stats/utils/GameUtils.kt
- app/src/main/java/com/joysticks/stats/utils/PdfExportUtils.kt

Sorties attendues:

- lecture/ecriture fiable
- format d'export exploitable

## 2. Chaines de traitement canonique

### Chaine A - Import alignement local

HomeScreen -> parseRoster -> TeamDataStore.loadTeams -> GameViewModel.loadRoster -> GameScreen

### Chaine B - Telechargement alignement distant

HomeScreen -> AlignementRepository.downloadRoster -> parseRoster -> GameViewModel.loadRoster -> GameScreen

### Chaine C - Conduite de match

GameScreen/BattingScreen/OpponentScoreScreen -> GameViewModel -> GameState -> rendu Compose

### Chaine D - Correction feuille de match

StatsSheetScreen -> route game avec event cible -> BattingScreen -> GameViewModel.editAtBatEvent -> StatsSheetScreen

### Chaine E - Exports

CSV: GameViewModel.generateCsv
PDF: MainActivity callback -> PdfExportUtils.exportStatsSheetToPdf

## 3. Regles de travail agent

- Ne pas modifier plusieurs niveaux a la fois sans justification.
- Priorite aux changements minimaux dans le niveau cible.
- Toute evolution du niveau 4 doit etre revalidee depuis le niveau 2.
- Interdiction de dupliquer un contenu documentaire: appliquer `docs/00-gouvernance/regle-anti-doublons.md`.
- Eviter de modifier le fichier legacy suivant, sauf demande explicite:
  - engine/GameViewModel.kt

## 4. Compatibilite Gemini

AGENTS.md est un fichier Markdown valide et lisible par Gemini.

Limite importante:

- selon l'outil Gemini utilise, AGENTS.md n'est pas toujours injecte automatiquement dans le contexte.

Recommandation:

- si ton environnement Gemini ne charge pas AGENTS.md automatiquement, ajouter le fichier explicitement au prompt ou copier ce contenu dans la configuration d'instructions du projet pour Gemini.

## 5. Documentation applicative obligatoire

- `docs/10-application/contexte-application.md`
- `docs/10-application/specification-fonctionnelle.md`
- `docs/10-application/architecture-specifique.md`
- `docs/10-application/specifications-fonctionnelles/*.md`

# Architecture de reference application Android Kotlin

## Portee

Architecture technique de reference de JoysticksStats pour Android Kotlin Compose.
Ce document couvre uniquement l'implementation (code, composants, flux techniques).
L'architecture fonctionnelle est centralisee dans:
`docs/10-application/architecture-specifique.md`.

## Couches techniques

- Bootstrap Android (Activity + Theme)
- Navigation Compose
- Ecrans UI
- Moteur metier et etat
- Adaptateurs de donnees
- Sorties CSV/PDF

## Composants et responsabilites

- `MainActivity`: entree Android + callback export PDF.
- `AppNavigation`: routage ecrans.
- `GameScreen` et ecrans metier: interactions utilisateur.
- `GameViewModel`: regles baseball et transitions d'etat.
- `GameState`: source de verite immutable.
- `RosterParser`/`AlignementRepository`/`TeamDataStore`: donnees.
- `PdfExportUtils`: export PDF.

## Flux techniques

- Import local: picker -> parse -> chargement roster -> rendu ecran.
- Import distant: HTTP -> parse -> chargement roster.
- Match: action UI -> transition d'etat -> rerender Compose.
- Edition feuille: selection event -> correction -> mise a jour etat.
- Export: CSV via moteur, PDF via callback Activity.

## Contraintes d'implementation

- `GameState` unique source de verite.
- Regles critiques dans `GameViewModel`.
- Les ecrans ne portent pas de logique metier critique.

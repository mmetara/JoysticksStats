# Specification detaillee - Conduite de match

## Cas d'usage

- Demarrer un match avec alignement charge.
- Saisir les actions de frappe.
- Saisir les points adverses.
- Corriger un evenement de feuille de match.
- Cloturer la partie (Mode GAME_OVER).
- Reprendre une partie en cours ou consulter un match terminé depuis l'accueil.

## Regles metier propres au domaine

- Match borne a 9 manches (configurable).
- Limite de 3 points avant la 9e manche.
- Manche ouverte a partir de la 9e.
- Gestion des coureurs par base et historique d'evenements.
- **Persistance** : L'état est sauvegardé à chaque action. Un match est considéré "en cours" s'il y a un alignement et une progression (manche > 1 ou historique non vide).
- **Sécurité de fin de match** : En mode GAME_OVER, la suppression de la sauvegarde nécessite une confirmation explicite pour éviter la perte accidentelle des données avant export.

## Ecrans a livrer

- `GameScreen`
- `BattingScreen`
- `OpponentScoreScreen`
- `StatsSheetScreen`

## Donnees affichees et modifiees

- score local/visiteur,
- inning et demi-manche,
- outs,
- occupation des bases,
- historique `AtBatEvent`.

## Tracabilite des demandes prompt

| Demande prompt | Fichier(s) principal(aux) | Regle impactee | Verification |
|---|---|---|---|
| Documentation structuree par niveaux | `AGENTS.md`, `GameViewModel`, `GameState` | separer niveau UI/metier | coherence flux D |

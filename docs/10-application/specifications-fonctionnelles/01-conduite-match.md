# Specification detaillee - Conduite de match

## Cas d'usage

- Demarrer un match avec alignement charge.
- Saisir les actions de frappe.
- Saisir les points adverses.
- Corriger un evenement de feuille de match.
- Cloturer la partie.

## Regles metier propres au domaine

- Match borne a 9 manches.
- Limite de 3 points avant la 9e manche.
- Manche ouverte a partir de la 9e.
- Gestion des coureurs par base et historique d'evenements.

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

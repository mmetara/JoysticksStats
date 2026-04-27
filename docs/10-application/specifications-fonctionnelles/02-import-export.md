# Specification detaillee - Import et export

## Cas d'usage

- Importer un alignement depuis un fichier local.
- Telecharger un alignement distant.
- Exporter les stats en CSV.
- Exporter la feuille en PDF.

## Regles metier propres au domaine

- L'alignement charge initialise le contexte de match.
- Le mapping id equipe -> nom equipe utilise le DataStore local.
- Le CSV exporte reprend stats frappeurs et total points.
- Le PDF est genere a partir de l'etat courant.

## Ecrans a livrer

- `HomeScreen`
- `GameScreen` (zone fin de match)

## Donnees affichees et modifiees

- roster joueurs,
- infos match (date, heure, equipe locale/visiteur),
- stats individuelles et cumulatives,
- fichier export cible (URI utilisateur).

## Tracabilite des demandes prompt

| Demande prompt | Fichier(s) principal(aux) | Regle impactee | Verification |
|---|---|---|---|
| Documentation structuree par niveaux | `HomeScreen`, `RosterParser`, `PdfExportUtils` | chaine import/export explicite | coherence flux B/C/F |

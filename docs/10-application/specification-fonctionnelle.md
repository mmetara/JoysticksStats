# Specification fonctionnelle (index)

Ce fichier est l'index de lecture. Les specifications detaillees sont decoupees par domaine dans `specifications-fonctionnelles/`.

## Perimetre global

- Objectif metier global: conduite complete d'un match de baseball.
- Acteurs principaux: scoreur/coach.
- Contraintes transverses: vitesse de saisie, fiabilite metier, exportabilite des donnees.

## Index des specifications detaillees

1. [Conduite de match](specifications-fonctionnelles/01-conduite-match.md) - deroulement inning par inning et correction d'evenements.
2. [Import et export](specifications-fonctionnelles/02-import-export.md) - alimentation roster et sorties CSV/PDF.

## Regles transverses communes

- L'etat de match est centralise.
- Les regles metier critiques ne sont pas portees par les ecrans.
- Toute nouvelle regle est tracee dans la specification detaillee correspondante.

## Tracabilite des demandes prompt

| Demande prompt | Specification detaillee | Implementation associee | Impact documentaire |
|---|---|---|---|
| Documentation multi-fichiers type socle | [Conduite de match](specifications-fonctionnelles/01-conduite-match.md) | `GameScreen`, `BattingScreen`, `GameViewModel` | creation structure 00/01/10/80 + docs/10-application |
| Documentation multi-fichiers type socle | [Import et export](specifications-fonctionnelles/02-import-export.md) | `HomeScreen`, `RosterParser`, `PdfExportUtils` | index et flux canoniques mis a jour |

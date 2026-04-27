# Architecture specifique application - JoysticksStats

## Portee documentaire

Ce document couvre uniquement l'architecture fonctionnelle metier (quoi et pourquoi).
Les details techniques Android/Kotlin (comment) sont centralises dans:
`docs/20-android-kotlin-technique/architecture-de-reference-application-android-kotlin.md`.

## Capacites fonctionnelles

- Initialiser un match a partir d'un alignement local ou distant.
- Piloter la conduite du match inning par inning.
- Corriger un evenement de feuille de match apres saisie.
- Exporter les resultats en CSV et PDF.

## Frontieres fonctionnelles

- Contexte pre-match: alignement, equipes, metadata de partie.
- Conduite de match: score, manches, bases, historique d'evenements.
- Fin de match: consolidation des statistiques et sortie des exports.

## Chaines fonctionnelles de reference

- Import local d'alignement.
- Import distant d'alignement.
- Conduite de match en direct.
- Correction de feuille de match.
- Exports CSV/PDF.

Les details pas-a-pas de ces chaines sont maintenus dans:
`docs/10-application/specification-fonctionnelle.md` et
`docs/10-application/specifications-fonctionnelles/*.md`.

## Contraintes metier

- Le systeme doit rester rapide en situation de match.
- Les regles de score doivent rester coherentes de bout en bout.
- Les exports doivent refleter l'etat final valide de la partie.

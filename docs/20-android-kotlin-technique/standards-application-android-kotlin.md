# Standards application Android Kotlin

## Portee

Ce document porte uniquement les standards techniques Android/Kotlin.
Les regles fonctionnelles metier sont maintenues dans `docs/10-application/`.

## Lecture rapide agent IA

1. Lire `docs/00-gouvernance/contrat-collaboration-ia.md`.
2. Lire `docs/01-vue-ensemble-solution/cartographie-solution.md`.
3. Identifier le niveau cible (0..5).
4. Appliquer `AGENTS.md` pour la chaine impactee.

## Standards techniques minimum obligatoires

- Navigation centralisee dans `AppNavigation`.
- Utiliser les packages `ui/screens`, `ui/components`, `ui/navigation`, `engine`, `data`, `utils`.
- Les donnees permanentes transitent par les adaptateurs dedies (`TeamDataStore`, parseurs, repository).
- Les sorties passent par les utilitaires dedies d'export.

## Regles de nommage et structure

- Garder les packages sous `com.joysticks.stats`.
- Respecter l'arborescence de reference:
  - `docs/20-android-kotlin-technique/arborescence-reference-application-android-kotlin.md`

## Points de verite lies

- Architecture technique: `docs/20-android-kotlin-technique/architecture-de-reference-application-android-kotlin.md`
- Specification fonctionnelle: `docs/10-application/specification-fonctionnelle.md`
- Checklist de livraison: `docs/20-android-kotlin-technique/liste-verification-implantation-application-android-kotlin.md`

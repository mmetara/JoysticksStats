# Regle anti-doublons documentaire

## Objectif

Eviter toute duplication de contenu entre documents.

## Principe

Un sujet = un fichier de verite.
Tous les autres documents doivent faire un renvoi vers ce fichier au lieu de recopier le contenu.

## Matrice de propriete (canonique)

- Contexte metier: `docs/10-application/contexte-application.md`
- Specification fonctionnelle globale: `docs/10-application/specification-fonctionnelle.md`
- Specifications detaillees: `docs/10-application/specifications-fonctionnelles/*.md`
- Architecture fonctionnelle (quoi/pourquoi): `docs/10-application/architecture-specifique.md`
- Architecture technique Android (comment): `docs/20-android-kotlin-technique/architecture-de-reference-application-android-kotlin.md`
- Standards techniques Android: `docs/20-android-kotlin-technique/standards-application-android-kotlin.md`
- Arborescence technique Android: `docs/20-android-kotlin-technique/arborescence-reference-application-android-kotlin.md`
- Guide d'execution locale: `docs/20-android-kotlin-technique/guide-execution-locale-application-android-kotlin.md`
- Checklist d'implantation technique: `docs/20-android-kotlin-technique/liste-verification-implantation-application-android-kotlin.md`
- Gouvernance des regles IA: `docs/00-gouvernance/contrat-collaboration-ia.md`

## Regles d'ecriture obligatoires

1. Interdit de dupliquer un bloc de regles dans un autre fichier.
2. Si l'information existe deja, remplacer par un renvoi explicite vers la source canonique.
3. Si un nouveau sujet apparait, definir son fichier proprietaire avant de rediger.
4. Chaque PR documentaire doit verifier l'absence de doublon semantique.

## Controle avant validation

- Verifier qu'un meme sujet n'est pas decrit en detail dans plusieurs fichiers.
- Verifier que les fichiers non canoniques contiennent un renvoi, pas une re-ecriture.
- Mettre a jour cette matrice si un nouveau domaine documentaire est introduit.

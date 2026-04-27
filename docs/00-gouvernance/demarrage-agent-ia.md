# Checklist de support agent IA

Ce document est une checklist de support. En cas d'ecart, le contrat prevaut:
`docs/00-gouvernance/contrat-collaboration-ia.md`.

## Avant de coder

- [ ] Le contrat est lu en entier.
- [ ] Le routage documentaire est fait via `docs/00-gouvernance/table-matieres-structures.json`.
- [ ] La matrice anti-doublons est lue via `docs/00-gouvernance/regle-anti-doublons.md`.
- [ ] Le niveau cible (0 a 5) est identifie.
- [ ] Les documents techniques/fonctionnels a mettre a jour sont identifies.

## Pendant le travail

- [ ] Les changements restent dans le niveau cible.
- [ ] Les chaines fonctionnelles impactees sont mises a jour.
- [ ] Les incoherences entre routing/navigation et docs sont corrigees.

## Avant de terminer

- [ ] Les impacts code sont traces dans la doc.
- [ ] Aucun sujet n'a ete duplique; les fichiers non canoniques pointent vers la source de verite.
- [ ] La specification fonctionnelle indexe bien les fichiers detaillees.
- [ ] La cartographie solution et AGENTS restent coherents.

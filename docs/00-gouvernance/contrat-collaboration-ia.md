# Contrat de collaboration IA - JoysticksStats

## But

Fournir un cadre documentaire stable, actionnable et versionne pour guider les evolutions de JoysticksStats.

## Ordre de commandement decisionnel

1. En cas de decision structurante, l'agent stoppe et demande validation explicite.
2. Une seule option d'arbitrage est proposee a la fois.
3. Aucune action irreversible sans feu vert explicite.
4. Toute decision d'architecture impactante est tracee dans la documentation.

## Regles de documentation

- Source de verite unique par sujet.
- Avant toute reponse de process (socle, methode, workflow), l'agent lit les documents obligatoires du demarrage projet.
- Toute modification code impactant le comportement metier met a jour la documentation dans le meme changement.
- Interdiction de dupliquer un meme contenu sur plusieurs fichiers. En cas de recouvrement, conserver une seule source canonique et remplacer ailleurs par un renvoi.
- Le decoupage documentaire suit la structure:
  - `docs/00-gouvernance/`
  - `docs/01-vue-ensemble-solution/`
  - `docs/20-android-kotlin-technique/`
  - `docs/80-entree-projet/`
  - `docs/10-application/`
- La regle d'application anti-doublons est definie dans `docs/00-gouvernance/regle-anti-doublons.md`.
- Le fichier `docs/10-application/specification-fonctionnelle.md` est obligatoire et sert d'index vers les specifications detaillees.
- Les specifications detaillees vivent dans `docs/10-application/specifications-fonctionnelles/*.md`.

## Demarrage agent obligatoire

Avant toute reponse de process, l'agent doit lire dans cet ordre:

1. `docs/00-gouvernance/contrat-collaboration-ia.md`
2. `docs/00-gouvernance/table-matieres-structures.json`
3. `README.md`
4. `docs/01-vue-ensemble-solution/cartographie-solution.md`
5. `docs/20-android-kotlin-technique/standards-application-android-kotlin.md`
6. `docs/10-application/specification-fonctionnelle.md`

## Regles d'implementation

- Ne pas modifier plusieurs niveaux (0..5) sans justification.
- Toute evolution du coeur metier (niveau 4) doit etre revalidee depuis les ecrans (niveau 2).
- Priorite aux changements minimaux dans le niveau cible.
- Le fichier legacy `engine/GameViewModel.kt` (hors `app/`) ne doit pas etre modifie sans demande explicite.

## Exigences non negociables

- Les regles de baseball critiques vivent dans `app/src/main/java/com/joysticks/stats/engine/GameViewModel.kt` et non dans les ecrans.
- `GameState` reste la source de verite de la partie.
- Toute nouvelle chaine fonctionnelle est documentee dans la cartographie solution et la specification fonctionnelle.

# AGENTS.md — joutio-theme

Sujets (topics) et questions : création, atelier collaboratif, modération, signalements,
publication, tirage des questions, difficulté calculée, seed du catalogue. Package
`com.joutio.theme`, base `joutio_theme`, port local 8086. Service headless.

Lire : `../../AGENTS.md`, [`docs/backend/`](../../docs/backend/hexagonal.md), exposition dans
[`docs/api/sujets.md`](../../docs/api/sujets.md) et [`docs/api/admin.md`](../../docs/api/admin.md).

## Agrégats et règles

- **`TopicAggregate`** : noms FR/EN, description, univers (17 `TopicCategory`), vignette,
  bannière, pictogramme, titres par palier, équipe, statut `DRAFT|PUBLISHED`, badge Officiel,
  fermeture par la modération.
  - Équipe : `CREATOR` (protégé), `ADMIN`, `WRITER`. Les droits Premium sont préparés par le BFF.
  - **Publication** : créateur ou admin, **7 questions approuvées dans une même langue**
    (`countPlayableByTopicId`). Un total toutes langues publierait un sujet injouable.
  - `TopicAvailabilityPolicy` repasse le sujet en brouillon sous ce seuil. Un brouillon bloque les
    nouveaux départs (bot, défi, salon, appariement) ; les parties créées continuent.
  - Un sujet fermé reste brouillon jusqu'à sa réouverture (`urn:joutio:topic:closed`).
  - Suppression d'un brouillon seulement, par le créateur (`TopicDeletionPolicy` supprime ses
    questions).
- **`QuestionAggregate`** : contenus localisés (texte ≤ 255, 4 réponses, bonne réponse présente
  dans chaque langue), image, difficulté, auteur.
  - Statut unique `PENDING|APPROVED|REJECTED|SUSPENDED|ARCHIVED`. Seules les `APPROVED` sont
    jouables. Motifs, notes et dates servent à l'audit.
  - Archiver est réversible, restaurer revient à `PENDING`. Une question suspendue reste suspendue
    après correction jusqu'à une approbation explicite.
  - La suppression émet `QuestionDeletedEvent` et `markDeleted()` ; les snapshots des parties
    gardent leur contenu.
- **`QuestionReportThreadAggregate`** : un fil par paire question × signalant
  (`UUID(questionId|reporterId)`), un seul `OPEN`. Il fige un snapshot de la version jouée
  (`PLAYED`, sinon `CURRENT`).
- **Suspension automatique** (`QuestionReportSuspensionPolicy`, `@DisallowReplay`) :
  - **3** signaleurs distincts, ouverts ou clos sans modification, sur `P7D` ;
  - fenêtre comptée depuis `max(reportedAt - window, reinstatedAt, restoredAt)` ;
  - réglages `joutio.question-report.auto-suspension.*`.
- Éditions **champ par champ** et idempotentes (aucun événement sans changement). Constantes dans
  `TopicRules` (nom ≤ 255, description ≤ 500, URL ≤ 1024, titre ≤ 40) et `QuestionRules`.
- **Difficulté** : `QuestionDifficultyProjection` compte les réponses **humaines**
  (`QuestionAnsweredEvent` de game ; un timeout compte comme une erreur). Seuils 80/60/40 %, à
  partir de 10 réponses (`QuestionRules.difficultyFor`), puis `UpdateQuestionDifficultyCommand`.

## Lectures fournies (`joutio-theme-domain`)

- **Tirage** : `GetRandomApprovedQuestionsQuery(topicId, count, languages, difficulties, excludeQuestionIds)`
  (consommée par matchmaking).
  - Une question doit avoir un contenu dans **toutes** les langues demandées.
  - Difficulté et exclusions ne sont que des préférences d'ordre : jamais jouée, puis difficulté
    cible, puis inconnue, puis le reste.
  - Pas de dédoublonnage par contenu : deux questions au même énoncé restent distinctes.
- **Couverture linguistique** : `GetTopicLanguageCoverageQuery` (portée par le ticket
  d'appariement).
- **Catalogue** :
  - `GetTopicPageQuery`, tris `POPULAR|MOST_PLAYED|ALPHA|RECENT`. `RECENT` suit
    `questionsChangedAt` : publication ou changement du nombre de questions jouables ;
  - `TopicFacetsQuery`, `GetTopicsByIdsQuery`, `GetTopicsByCreatorQuery` ;
  - équipe et atelier ;
  - `CountImageReferencesQuery` (console médias) ;
  - `TopicSearchQuery` (admin).
- **Questions et signalements** : `QuestionQuery.*`, `GetQuestionAdminPageQuery`,
  `QuestionReportQuery.*`.
- **Recherche** insensible aux accents et à la casse (`name_normalized`, `SearchText.normalize`).
- **`games_counter`** : parties créées sur le sujet, bots compris (`TopicGamesProjection` sur
  `GameCreatedEvent`, table sans clé étrangère, rejouable). Sert au tri « Les plus joués ».

Contrat consommé : `joutio-game-domain`. Aucune requête sortante.

## Seed du catalogue

- Un YAML par sujet : `src/main/resources/seed/topics/<univers>/<sujet>.yml` (~100 questions).
  `app.seed-data.enabled`, `.location`, `.image-base-url`.
  - `topic` : `id` stable, `name`, `description`, `category`, `imageUrl?`, `pictogramUrl?`,
    `titles?` ;
  - `questions[]` : `text` (le français), `answers` A–D, `correctAnswer`, `imageUrl?`,
    `difficulty?`, `translations.en`.
  - Images en chemin relatif `wikipedia/<univers>/<fichier>`.
- `SeedDataLoader` valide chaque fichier ; un fichier invalide est ignoré sans bloquer les autres.
- `DataSeeder` est **auto-réparateur** et idempotent :
  - il complète les questions, traductions et difficultés manquantes ;
  - il approuve et publie ;
  - clé de réparation : `(text, QuestionIdentity.imageKey)`, la fin du chemin de l'image sans la
    base d'URL ;
  - une décision éditoriale (brouillon, suspension, archivage, suppression) verrouille la
    complétion automatique du sujet (`topic_seed_lock`) ;
  - échec transitoire : jusqu'à 4 tentatives par sujet ; métriques
    `joutio_theme_seed_topics_total{outcome}` et `…_retries_total`.
- **Cadence** (`app.seed-data.pacing.*`, `SeedPacer`) : un seed complet est étalé (attente
  initiale, pause proportionnelle au temps des commandes, pause entre sujets).
- **Modifier** un contenu déjà publié (texte, réponses, couverture, base d'images) exige une
  remise à zéro coordonnée avec la release ([exploitation](../../docs/ops/exploitation.md#remise-à-zéro-des-bases)).
- **Règles de contenu**, vérifiées par des tests :
  - une licence ou une fiction par sujet ;
  - bonnes réponses réparties à ~25 % sur A–D ±1 (`SeedAnswerBalanceTest`,
    `rebalance-seed.py`) ;
  - vignette unique par sujet (`SeedTopicCoverUniquenessTest`) ;
  - traduction EN partout sauf `orthographe-grammaire` (`SeedEnTranslationCoverageTest`) ;
  - question imagée en ≤ 60 caractères, qui ne décrit pas l'image ;
  - répartition des difficultés (`SeedDifficultyBalanceTest`, cible 10/20/35/35).
- **Images** : libres seulement (Wikimedia Commons), versionnées dans `minio/joutio-media/wikipedia/` du workspace
  (crédits dans `minio/CREDITS.md`), publiées par `make seed-images`.
  - Jamais de logo, d'affiche ni de capture officielle.
  - **Exception actée** : les écussons de clubs (`sports/blasons-de-clubs.yml`), non libres,
    ajoutés à la main et marqués dans `CREDITS.md`.
- **Outillage** (`joutio-theme-infrastructure/src/main/resources/seed/`, API DeepSeek) : `generate-seed.py` → `translate-seed.py` →
  `rebalance-seed.py` ; `annotate-difficulty.py --check`.

## Commandes

```bash
mvn -q -pl joutio-theme-domain test
mvn -pl joutio-theme-infrastructure test
cd joutio-theme-infrastructure/src/main/resources/seed && python3 rebalance-seed.py --check topics/*/*.yml
```

## Pièges

- Les champs JSON `suspended` et `archived` des vues sont dérivés du statut. Ce n'est pas un second
  état.

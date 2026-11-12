-- 2026-11-12 : pictogramme carré de la tuile du catalogue, emoji et couleur supprimés (code mort),
-- catégories alignées sur le design (RELIGION retirée : ses sujets passent en culture générale).
ALTER TABLE topic_entry ADD COLUMN pictogram_url character varying(1024);
COMMENT ON COLUMN topic_entry.pictogram_url IS 'URL optionnelle du pictogramme carré de la tuile du catalogue';

ALTER TABLE topic_entry DROP COLUMN emoji;
ALTER TABLE topic_entry DROP COLUMN color;

UPDATE topic_entry SET category = 'GENERAL' WHERE category = 'RELIGION';
ALTER TABLE topic_entry DROP CONSTRAINT chk_topic_category;
ALTER TABLE topic_entry ADD CONSTRAINT chk_topic_category CHECK (category IN (
    'ARTS', 'BUSINESS', 'EDUCATION', 'ENTERTAINMENT', 'FOOD_AND_DRINK', 'GAMES', 'GENERAL', 'HISTORY',
    'LITERATURE', 'MOVIES', 'MUSIC', 'NATURE', 'SCIENCE', 'SPORTS', 'TELEVISION', 'TECHNOLOGY', 'WORLD'));

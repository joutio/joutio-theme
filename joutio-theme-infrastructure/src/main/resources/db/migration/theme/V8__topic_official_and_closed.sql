-- 2026-11-15 : badge « Officiel » (sujet éditorial de Joutio) et fermeture par la modération (notification de
-- contenu illicite, LCEN/DSA). Les sujets du seed, créés par le compte système, deviennent officiels.
ALTER TABLE topic_entry ADD COLUMN official boolean NOT NULL DEFAULT false;
ALTER TABLE topic_entry ADD COLUMN closed boolean NOT NULL DEFAULT false;
ALTER TABLE topic_entry ADD COLUMN closed_reason varchar(500);
UPDATE topic_entry SET official = true WHERE creator_id = '0ada9a20-2198-4014-9ed0-57d0cc82fb42';
COMMENT ON COLUMN topic_entry.official IS 'Badge Officiel : sujet éditorial de Joutio, posé par un administrateur';
COMMENT ON COLUMN topic_entry.closed IS 'Fermé par la modération : publication refusée jusqu''à réouverture';
CREATE INDEX IF NOT EXISTS idx_topic_entry_official ON topic_entry (official) WHERE official;

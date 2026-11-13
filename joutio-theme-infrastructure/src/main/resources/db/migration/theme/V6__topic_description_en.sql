-- 2026-11-13 : description localisée, comme le nom. `description` reste la description française.
ALTER TABLE topic_entry ADD COLUMN description_en character varying(500);
COMMENT ON COLUMN topic_entry.description_en IS 'Description anglaise optionnelle (repli : description française)';

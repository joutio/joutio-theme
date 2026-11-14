-- 2026-11-14 : « Fraîchement mis à jour » reflète un ajout ou un retrait de question jouable, pas n'importe quelle
-- modification du thème. `updated_at` reste la dernière modification, quelle qu'elle soit.
ALTER TABLE topic_entry ADD COLUMN questions_changed_at timestamp without time zone;
UPDATE topic_entry SET questions_changed_at = COALESCE(updated_at, created_at);
COMMENT ON COLUMN topic_entry.questions_changed_at IS 'Dernier ajout ou retrait de question approuvée, ou publication du thème (tri RECENT)';

ALTER TABLE question_entry DROP CONSTRAINT chk_question_status;
ALTER TABLE question_entry ADD CONSTRAINT chk_question_status CHECK (status IN ('PENDING','APPROVED','REJECTED','SUSPENDED','ARCHIVED'));
ALTER TABLE topic_questions_counter DROP CONSTRAINT chk_topic_questions_status;
ALTER TABLE topic_questions_counter ADD CONSTRAINT chk_topic_questions_status CHECK (question_status IN ('PENDING','APPROVED','REJECTED','SUSPENDED','ARCHIVED'));
-- Convert legacy flags. Archived takes precedence over suspended.
UPDATE question_entry SET status='SUSPENDED' WHERE suspended=true AND archived=false;
UPDATE question_entry SET status='ARCHIVED' WHERE archived=true;
DELETE FROM topic_questions_counter;
INSERT INTO topic_questions_counter(topic_id,question_status,counter)
SELECT topic_id,status,count(*) FROM question_entry GROUP BY topic_id,status;

CREATE TABLE topic_seed_lock (topic_id varchar(255) PRIMARY KEY);
INSERT INTO topic_seed_lock(topic_id)
SELECT DISTINCT topic_id FROM question_entry WHERE status IN ('SUSPENDED','ARCHIVED');

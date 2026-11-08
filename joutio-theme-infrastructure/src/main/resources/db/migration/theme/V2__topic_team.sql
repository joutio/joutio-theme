CREATE TABLE topic_team_member (
 topic_id varchar(255) NOT NULL, user_id varchar(255) NOT NULL,
 role varchar(16) NOT NULL CHECK (role IN ('ADMIN','WRITER')),
 PRIMARY KEY(topic_id,user_id)
);
CREATE INDEX idx_topic_team_user ON topic_team_member(user_id);
ALTER TABLE question_entry ADD COLUMN rejection_reason varchar(1000);

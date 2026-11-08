package com.joutio.theme.infrastructure.out.persistence.adapter;

import com.joutio.theme.domain.port.out.TopicSeedPolicyPort;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TopicSeedPolicyAdapter implements TopicSeedPolicyPort {
    private final JdbcTemplate jdbc;

    public TopicSeedPolicyAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public boolean isLocked(String topicId) {
        return Boolean.TRUE.equals(
                jdbc.queryForObject(
                        "SELECT EXISTS(SELECT 1 FROM topic_seed_lock WHERE topic_id=?)",
                        Boolean.class,
                        topicId));
    }

    public void lock(String topicId) {
        jdbc.update(
                "INSERT INTO topic_seed_lock(topic_id) VALUES(?) ON CONFLICT DO NOTHING", topicId);
    }
}

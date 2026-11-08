package com.joutio.theme.infrastructure.out.persistence.adapter;

import com.joutio.theme.domain.model.TopicTeamRole;
import com.joutio.theme.domain.port.out.TopicTeamRepositoryPort;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class TopicTeamRepositoryAdapter implements TopicTeamRepositoryPort {
    private final JdbcTemplate jdbc;
    private final com.joutio.theme.domain.port.out.TopicRepositoryPort topics;

    public TopicTeamRepositoryAdapter(
            JdbcTemplate jdbc, com.joutio.theme.domain.port.out.TopicRepositoryPort topics) {
        this.jdbc = jdbc;
        this.topics = topics;
    }

    public Map<String, TopicTeamRole> members(String topicId) {
        Map<String, TopicTeamRole> result = new LinkedHashMap<>();
        jdbc.query(
                "SELECT creator_id FROM topic_entry WHERE topic_id = ?",
                rs -> {
                    result.put(rs.getString(1), TopicTeamRole.CREATOR);
                },
                topicId);
        jdbc.query(
                "SELECT user_id, role FROM topic_team_member WHERE topic_id = ? ORDER BY user_id",
                rs -> {
                    result.putIfAbsent(rs.getString(1), TopicTeamRole.valueOf(rs.getString(2)));
                },
                topicId);
        return result;
    }

    public void setMember(String topicId, String userId, TopicTeamRole role) {
        jdbc.update(
                "INSERT INTO topic_team_member(topic_id,user_id,role) VALUES (?,?,?) ON"
                        + " CONFLICT(topic_id,user_id) DO UPDATE SET role=excluded.role",
                topicId,
                userId,
                role.name());
    }

    public void removeMember(String topicId, String userId) {
        jdbc.update(
                "DELETE FROM topic_team_member WHERE topic_id=? AND user_id=?", topicId, userId);
    }

    public com.joutio.theme.domain.model.TopicPage contributed(String userId, int page, int size) {
        var ids =
                jdbc.queryForList(
                        "SELECT t.topic_id FROM topic_entry t LEFT JOIN topic_team_member m ON"
                                + " m.topic_id=t.topic_id AND m.user_id=? WHERE t.creator_id=? OR"
                                + " m.user_id IS NOT NULL ORDER BY t.updated_at DESC NULLS"
                                + " LAST,t.topic_id LIMIT ? OFFSET ?",
                        String.class,
                        userId,
                        userId,
                        size,
                        (long) page * size);
        long total =
                jdbc.queryForObject(
                        "SELECT count(*) FROM topic_entry t LEFT JOIN topic_team_member m ON"
                                + " m.topic_id=t.topic_id AND m.user_id=? WHERE t.creator_id=? OR"
                                + " m.user_id IS NOT NULL",
                        Long.class,
                        userId,
                        userId);
        return new com.joutio.theme.domain.model.TopicPage(
                topics.findAllByIds(ids), page, size, total, (int) ((total + size - 1) / size));
    }
}

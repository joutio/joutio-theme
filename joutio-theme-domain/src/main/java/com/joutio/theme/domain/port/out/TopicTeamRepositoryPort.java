package com.joutio.theme.domain.port.out;

import com.joutio.theme.domain.model.TopicTeamRole;

import java.util.Map;

public interface TopicTeamRepositoryPort {
    Map<String, TopicTeamRole> members(String topicId);

    void setMember(String topicId, String userId, TopicTeamRole role);

    void removeMember(String topicId, String userId);

    com.joutio.theme.domain.model.TopicPage contributed(String userId, int page, int size);
}

package com.joutio.theme.application.projection;

import com.joutio.theme.domain.event.TopicEvent;
import com.joutio.theme.domain.port.out.TopicTeamRepositoryPort;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ProcessingGroup("theme-projection")
public class TopicTeamProjection {
    private final TopicTeamRepositoryPort teams;

    public TopicTeamProjection(TopicTeamRepositoryPort teams) {
        this.teams = teams;
    }

    @EventHandler
    @Transactional
    public void on(TopicEvent.TopicMemberSetEvent event) {
        teams.setMember(event.topicId(), event.userId(), event.role());
    }

    @EventHandler
    @Transactional
    public void on(TopicEvent.TopicMemberRemovedEvent event) {
        teams.removeMember(event.topicId(), event.userId());
    }

    @EventHandler
    @Transactional
    public void on(TopicEvent.TopicDeletedEvent event) {
        teams.members(event.topicId()).keySet().forEach(userId -> teams.removeMember(event.topicId(), userId));
    }
}

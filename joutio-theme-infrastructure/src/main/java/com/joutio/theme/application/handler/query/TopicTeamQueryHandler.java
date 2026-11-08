package com.joutio.theme.application.handler.query;

import com.joutio.theme.domain.model.TopicTeam;
import com.joutio.theme.domain.port.out.TopicTeamRepositoryPort;
import com.joutio.theme.domain.query.TopicQuery;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class TopicTeamQueryHandler {
    private final TopicTeamRepositoryPort teams;

    public TopicTeamQueryHandler(TopicTeamRepositoryPort teams) {
        this.teams = teams;
    }

    @QueryHandler
    public TopicTeam handle(TopicQuery.GetTopicTeamQuery query) {
        return new TopicTeam(teams.members(query.topicId()));
    }

    @QueryHandler
    public com.joutio.theme.domain.model.TopicPage handle(
            TopicQuery.GetContributedTopicsQuery query) {
        return teams.contributed(query.userId(), query.page(), query.size());
    }
}

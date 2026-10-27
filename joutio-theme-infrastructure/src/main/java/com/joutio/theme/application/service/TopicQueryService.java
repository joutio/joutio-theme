package com.joutio.theme.application.service;

import com.joutio.microservice.core.infrastructure.axon.QueryResponseTypes;
import com.joutio.microservice.core.infrastructure.in.api.response.SearchResponse;
import com.joutio.theme.domain.exception.TopicProblems;
import com.joutio.theme.domain.model.Topic;
import com.joutio.theme.domain.port.in.CheckTopicUseCase;
import com.joutio.theme.domain.port.in.GetTopicUseCase;
import com.joutio.theme.domain.port.in.SearchTopicUseCase;
import com.joutio.theme.domain.query.TopicQuery;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class TopicQueryService implements GetTopicUseCase, SearchTopicUseCase, CheckTopicUseCase {

    private final QueryGateway queryGateway;

    public TopicQueryService(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @Override
    public CompletableFuture<Topic> getById(TopicQuery.GetTopicByIdQuery query) throws TopicProblems.TopicNotFoundProblem {
        return queryGateway.query(query, QueryResponseTypes.instanceOf(Topic.class));
    }

    @Override
    public CompletableFuture<Boolean> existsById(TopicQuery.TopicExistsByIdQuery query) {
        return queryGateway.query(query, QueryResponseTypes.instanceOf(Boolean.class));
    }

    @Override
    public CompletableFuture<SearchResponse<Topic>> search(TopicQuery.TopicSearchQuery query) {
        return queryGateway.query(query, QueryResponseTypes.searchResponseOf(Topic.class));
    }
}

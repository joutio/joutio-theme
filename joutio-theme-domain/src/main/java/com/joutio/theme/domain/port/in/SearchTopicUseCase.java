package com.joutio.theme.domain.port.in;

import com.joutio.microservice.core.infrastructure.in.api.request.SearchRequest;
import com.joutio.microservice.core.infrastructure.in.api.response.SearchResponse;
import com.joutio.theme.domain.model.Topic;
import com.joutio.theme.domain.query.TopicQuery;

import java.util.concurrent.CompletableFuture;

public interface SearchTopicUseCase {

    CompletableFuture<SearchResponse<Topic>> search(TopicQuery.TopicSearchQuery query);

    default CompletableFuture<SearchResponse<Topic>> search(SearchRequest request) {
        return search(new TopicQuery.TopicSearchQuery(request));
    }
}

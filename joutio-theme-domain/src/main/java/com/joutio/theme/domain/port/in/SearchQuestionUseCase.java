package com.joutio.theme.domain.port.in;

import com.joutio.microservice.core.infrastructure.in.api.request.SearchRequest;
import com.joutio.microservice.core.infrastructure.in.api.response.SearchResponse;
import com.joutio.theme.domain.model.Question;
import com.joutio.theme.domain.query.QuestionQuery;

import java.util.concurrent.CompletableFuture;

public interface SearchQuestionUseCase {

    CompletableFuture<SearchResponse<Question>> search(QuestionQuery.QuestionSearchQuery query);

    default CompletableFuture<SearchResponse<Question>> search(SearchRequest request) {
        return search(new QuestionQuery.QuestionSearchQuery(request));
    }
}

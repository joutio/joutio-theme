package com.joutio.theme.application.service;

import com.joutio.microservice.core.infrastructure.axon.QueryResponseTypes;
import com.joutio.microservice.core.infrastructure.in.api.response.SearchResponse;
import com.joutio.theme.domain.exception.QuestionProblems;
import com.joutio.theme.domain.model.Question;
import com.joutio.theme.domain.port.in.GetQuestionUseCase;
import com.joutio.theme.domain.port.in.SearchQuestionUseCase;
import com.joutio.theme.domain.query.QuestionQuery;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class QuestionQueryService implements GetQuestionUseCase, SearchQuestionUseCase {

    private final QueryGateway queryGateway;

    public QuestionQueryService(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @Override
    public CompletableFuture<SearchResponse<Question>> search(QuestionQuery.QuestionSearchQuery query) {
        return queryGateway.query(query, QueryResponseTypes.searchResponseOf(Question.class));
    }

    @Override
    public CompletableFuture<Question> getById(QuestionQuery.GetQuestionByIdQuery query) throws QuestionProblems.QuestionNotFoundProblem {
        return queryGateway.query(query, QueryResponseTypes.instanceOf(Question.class));
    }

    @Override
    public CompletableFuture<List<Question>> getByTopicId(QuestionQuery.GetQuestionsByTopicIdQuery query) {
        return queryGateway.query(query, QueryResponseTypes.multipleInstancesOf(Question.class));
    }
}


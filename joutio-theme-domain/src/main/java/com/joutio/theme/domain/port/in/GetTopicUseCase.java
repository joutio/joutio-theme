package com.joutio.theme.domain.port.in;

import com.joutio.theme.domain.exception.TopicProblems;
import com.joutio.theme.domain.model.Topic;
import com.joutio.theme.domain.query.TopicQuery;

import java.util.concurrent.CompletableFuture;

public interface GetTopicUseCase {
    CompletableFuture<Topic> getById(TopicQuery.GetTopicByIdQuery query) throws TopicProblems.TopicNotFoundProblem;

    default CompletableFuture<Topic> getById(String topicId) throws TopicProblems.TopicNotFoundProblem {
        return getById(
                new TopicQuery.GetTopicByIdQuery(
                        topicId
                )
        );
    }
}


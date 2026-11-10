package com.joutio.theme.application.policy;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.joutio.theme.domain.command.QuestionCommand;
import com.joutio.theme.domain.event.TopicEvent;
import com.joutio.theme.domain.model.Question;
import com.joutio.theme.domain.port.out.QuestionRepositoryPort;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.DisallowReplay;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

class TopicDeletionPolicyTest {
    @Test
    void deletingADraftDeletesEachOfItsQuestions() {
        var questions = mock(QuestionRepositoryPort.class);
        var commands = mock(CommandGateway.class);
        when(questions.findByTopicId("topic"))
                .thenReturn(List.of(Question.builder().questionId("q1").build(), Question.builder().questionId("q2").build()));

        new TopicDeletionPolicy(questions, commands).on(new TopicEvent.TopicDeletedEvent("topic", "owner", Instant.now()));

        verify(commands).send(new QuestionCommand.DeleteQuestionCommand("q1", "owner"));
        verify(commands).send(new QuestionCommand.DeleteQuestionCommand("q2", "owner"));
    }

    @Test
    void policyIsNotReplayed() {
        assertTrue(TopicDeletionPolicy.class.isAnnotationPresent(DisallowReplay.class));
    }
}

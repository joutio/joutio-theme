package com.joutio.theme.application.projection;

import static org.mockito.Mockito.*;

import com.joutio.theme.domain.command.TopicCommand;
import com.joutio.theme.domain.event.QuestionEvent;
import com.joutio.theme.domain.port.out.*;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.Test;

import java.time.Instant;

class TopicAvailabilityProjectionTest {
    @Test
    void deletingBelowMinimumDraftsTopic() {
        var questions = mock(QuestionRepositoryPort.class);
        var commands = mock(CommandGateway.class);
        var reports = mock(QuestionReportRepositoryPort.class);
        when(questions.countApprovedByTopicId("topic")).thenReturn(6);
        new TopicAvailabilityProjection(questions, commands, reports)
                .on(new QuestionEvent.QuestionDeletedEvent("q", "topic", "owner", Instant.now()));
        verify(commands)
                .send(new TopicCommand.DraftTopicCommand("topic", "INSUFFICIENT_QUESTIONS"));
    }

    @Test
    void deletingWithEnoughQuestionsDoesNotDraftTopic() {
        var questions = mock(QuestionRepositoryPort.class);
        var commands = mock(CommandGateway.class);
        var reports = mock(QuestionReportRepositoryPort.class);
        when(questions.countApprovedByTopicId("topic")).thenReturn(7);
        new TopicAvailabilityProjection(questions, commands, reports)
                .on(new QuestionEvent.QuestionDeletedEvent("q", "topic", "owner", Instant.now()));
        verifyNoInteractions(commands);
    }

    @Test
    void deletionRemovesProjectionThroughPort() {
        var questions = mock(QuestionRepositoryPort.class);
        new QuestionProjection(questions)
                .on(new QuestionEvent.QuestionDeletedEvent("q", "topic", "owner", Instant.now()));
        verify(questions).deleteById("q");
        verifyNoMoreInteractions(questions);
    }
}

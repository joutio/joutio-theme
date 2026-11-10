package com.joutio.theme.application.policy;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.joutio.theme.application.projection.QuestionProjection;
import com.joutio.theme.domain.command.TopicCommand;
import com.joutio.theme.domain.event.QuestionEvent;
import com.joutio.theme.domain.model.Question;
import com.joutio.theme.domain.model.QuestionSuspensionReason;
import com.joutio.theme.domain.port.out.*;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.DisallowReplay;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

class TopicAvailabilityPolicyTest {
    private final QuestionRepositoryPort questions = mock(QuestionRepositoryPort.class);
    private final CommandGateway commands = mock(CommandGateway.class);
    private final QuestionReportRepositoryPort reports = mock(QuestionReportRepositoryPort.class);
    private final TopicAvailabilityPolicy policy = new TopicAvailabilityPolicy(questions, commands, reports);

    @Test
    void deletingBelowMinimumDraftsTopic() {
        when(questions.countApprovedByTopicIdExcluding("topic", "q")).thenReturn(6);
        policy.on(new QuestionEvent.QuestionDeletedEvent("q", "topic", "owner", Instant.now()));
        verify(commands).send(new TopicCommand.DraftTopicCommand("topic", "INSUFFICIENT_QUESTIONS"));
    }

    @Test
    void deletingWithEnoughQuestionsDoesNotDraftTopic() {
        when(questions.countApprovedByTopicIdExcluding("topic", "q")).thenReturn(7);
        policy.on(new QuestionEvent.QuestionDeletedEvent("q", "topic", "owner", Instant.now()));
        verifyNoInteractions(commands);
    }

    @Test
    void suspendingTheSeventhApprovedQuestionDraftsTopic_evenBeforeTheProjectionUpdates() {
        // Le read model compte encore la question comme approuvée : elle est exclue explicitement.
        when(questions.findById("q")).thenReturn(Optional.of(Question.builder().questionId("q").topicId("topic").build()));
        when(questions.countApprovedByTopicIdExcluding("topic", "q")).thenReturn(6);
        policy.on(new QuestionEvent.QuestionSuspendedEvent(
                "q", QuestionSuspensionReason.AUTO_REPORT_THRESHOLD, "note", "system", "r", Instant.now(), 3, 7, Instant.now()));
        verify(commands).send(new TopicCommand.DraftTopicCommand("topic", "INSUFFICIENT_QUESTIONS"));
    }

    @Test
    void policyIsNotReplayed() {
        assertTrue(TopicAvailabilityPolicy.class.isAnnotationPresent(DisallowReplay.class));
    }

    @Test
    void deletionRemovesProjectionThroughPort() {
        new QuestionProjection(questions)
                .on(new QuestionEvent.QuestionDeletedEvent("q", "topic", "owner", Instant.now()));
        verify(questions).deleteById("q");
        verifyNoMoreInteractions(questions);
    }
}

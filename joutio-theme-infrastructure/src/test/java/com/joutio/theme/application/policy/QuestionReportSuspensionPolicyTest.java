package com.joutio.theme.application.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.joutio.microservice.core.domain.model.i18n.Language;
import com.joutio.theme.domain.command.QuestionCommand;
import com.joutio.theme.domain.event.QuestionReportEvent;
import com.joutio.theme.domain.model.Question;
import com.joutio.theme.domain.model.QuestionChoice;
import com.joutio.theme.domain.model.QuestionContent;
import com.joutio.theme.domain.model.QuestionReportReason;
import com.joutio.theme.domain.model.QuestionStatus;
import com.joutio.theme.domain.port.out.QuestionReportRepositoryPort;
import com.joutio.theme.domain.port.out.QuestionRepositoryPort;
import com.joutio.theme.domain.model.QuestionAutoSuspension;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.DisallowReplay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

class QuestionReportSuspensionPolicyTest {

    private static final Instant NOW = Instant.parse("2026-10-17T12:00:00Z");

    private final QuestionReportRepositoryPort reportRepository =
            mock(QuestionReportRepositoryPort.class);
    private final QuestionRepositoryPort questionRepository = mock(QuestionRepositoryPort.class);
    private final CommandGateway commandGateway = mock(CommandGateway.class);
    private final QuestionReportSuspensionPolicy policy =
            new QuestionReportSuspensionPolicy(
                    reportRepository,
                    questionRepository,
                    commandGateway,
                    new QuestionAutoSuspension(true, 3, Duration.ofDays(7)));

    @BeforeEach
    void setUp() {
        when(questionRepository.findById("question-1")).thenReturn(Optional.of(question()));
    }

    @Test
    void thirdDistinctReporter_suspendsTheQuestion_withEventTimes() {
        when(reportRepository.countDistinctUnresolvedReportersSince(eq("question-1"), any(), eq("reporter-3")))
                .thenReturn(2);

        policy.on(reported());

        ArgumentCaptor<QuestionCommand.SuspendQuestionCommand> captor =
                ArgumentCaptor.forClass(QuestionCommand.SuspendQuestionCommand.class);
        verify(commandGateway).send(captor.capture());
        assertEquals("question-1", captor.getValue().questionId());
        assertEquals(3, captor.getValue().distinctReporters());
        assertEquals(NOW, captor.getValue().suspendedAt());
    }

    @Test
    void currentReporterIsCountedOnce_whateverTheHandlerOrder() {
        // La projection a pu enregistrer le signalement avant la politique : il est exclu du compte en base.
        when(reportRepository.countDistinctUnresolvedReportersSince(eq("question-1"), any(), eq("reporter-3")))
                .thenReturn(1);

        policy.on(reported());

        verify(commandGateway, never()).send(any());
    }

    @Test
    void windowStartsSevenDaysBeforeTheReport() {
        policy.on(reported());

        verify(reportRepository)
                .countDistinctUnresolvedReportersSince("question-1", NOW.minus(Duration.ofDays(7)), "reporter-3");
    }

    @Test
    void archivedQuestion_isNotSuspended() {
        Question archived = question().toBuilder().status(QuestionStatus.ARCHIVED).build();
        when(questionRepository.findById("question-1")).thenReturn(Optional.of(archived));

        policy.on(reported());

        verify(commandGateway, never()).send(any());
    }

    @Test
    void disabled_doesNothing() {
        new QuestionReportSuspensionPolicy(
                        reportRepository, questionRepository, commandGateway, QuestionAutoSuspension.DISABLED)
                .on(reported());

        verifyNoInteractions(commandGateway, reportRepository);
    }

    @Test
    void policyIsNotReplayed() {
        assertTrue(QuestionReportSuspensionPolicy.class.isAnnotationPresent(DisallowReplay.class));
    }

    private static QuestionReportEvent.QuestionReportedEvent reported() {
        return new QuestionReportEvent.QuestionReportedEvent(
                "thread-1",
                "report-3",
                "question-1",
                "topic-1",
                "creator-1",
                "reporter-3",
                "game-1",
                Set.of(QuestionReportReason.ANSWERS),
                "Bonne réponse fausse",
                Language.FR,
                null,
                NOW);
    }

    private static Question question() {
        return Question.builder()
                .questionId("question-1")
                .topicId("topic-1")
                .contents(
                        Map.of(
                                Language.FR,
                                new QuestionContent(
                                        Language.FR,
                                        "Capitale de la France ?",
                                        Map.of(
                                                QuestionChoice.A,
                                                "Paris",
                                                QuestionChoice.B,
                                                "Lyon",
                                                QuestionChoice.C,
                                                "Marseille",
                                                QuestionChoice.D,
                                                "Lille"))))
                .correctAnswer(QuestionChoice.A)
                .status(QuestionStatus.APPROVED)
                .creatorId("creator-1")
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();
    }
}

package com.joutio.theme.application.projection;

import com.joutio.theme.domain.command.TopicCommand;
import com.joutio.theme.domain.event.QuestionEvent;
import com.joutio.theme.domain.port.out.QuestionRepositoryPort;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(100)
@ProcessingGroup("theme-projection")
public class TopicAvailabilityProjection {
    private final com.joutio.theme.domain.port.out.QuestionReportRepositoryPort reports;
    private final QuestionRepositoryPort questions;
    private final CommandGateway commands;

    public TopicAvailabilityProjection(
            QuestionRepositoryPort questions,
            CommandGateway commands,
            com.joutio.theme.domain.port.out.QuestionReportRepositoryPort reports) {
        this.questions = questions;
        this.commands = commands;
        this.reports = reports;
    }

    @EventHandler
    public void on(QuestionEvent.QuestionArchivedEvent event) {
        checkQuestion(event.questionId());
    }

    @EventHandler
    public void on(QuestionEvent.QuestionSuspendedEvent event) {
        checkQuestion(event.questionId());
    }

    @EventHandler
    public void on(QuestionEvent.QuestionRejectedEvent event) {
        checkQuestion(event.questionId());
    }

    private void checkQuestion(String questionId) {
        questions.findById(questionId).ifPresent(q -> check(q.topicId()));
    }

    private void check(String topicId) {
        if (questions.countApprovedByTopicId(topicId)
                < com.joutio.theme.domain.model.TopicRules.MIN_QUESTIONS_TO_PUBLISH)
            commands.send(new TopicCommand.DraftTopicCommand(topicId, "INSUFFICIENT_QUESTIONS"));
    }

    @EventHandler
    public void on(QuestionEvent.QuestionDeletedEvent event) {
        check(event.topicId());
        for (var report : reports.findOpenByQuestionId(event.questionId()))
            commands.send(
                    new com.joutio.theme.domain.command.QuestionReportCommand
                            .ResolveQuestionReportCommand(
                            report.threadId(),
                            report.reportId(),
                            com.joutio.theme.domain.model.QuestionReportResolution.RESOLVED,
                            event.requestedBy(),
                            "QUESTION_DELETED",
                            event.deletedAt()));
    }
}

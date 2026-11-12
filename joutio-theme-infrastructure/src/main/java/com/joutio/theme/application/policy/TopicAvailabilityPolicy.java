package com.joutio.theme.application.policy;

import com.joutio.theme.domain.command.QuestionReportCommand;
import com.joutio.theme.domain.command.TopicCommand;
import com.joutio.theme.domain.event.QuestionEvent;
import com.joutio.theme.domain.model.QuestionReportResolution;
import com.joutio.theme.domain.model.TopicRules;
import com.joutio.theme.domain.model.TopicStatus;
import com.joutio.theme.domain.port.out.TopicRepositoryPort;
import com.joutio.theme.domain.port.out.QuestionReportRepositoryPort;
import com.joutio.theme.domain.port.out.QuestionRepositoryPort;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.DisallowReplay;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Politique de disponibilité d'un sujet : sous {@link TopicRules#MIN_QUESTIONS_TO_PUBLISH} questions
 * approuvées <b>dans une même langue</b> (questions jouables), le sujet repasse en brouillon.
 *
 * <p>La question de l'événement est exclue du compte : le résultat ne dépend pas de l'ordre
 * d'exécution avec {@code QuestionProjection}. {@code @Order(100)} reste nécessaire pour la
 * suppression, afin de lire les signalements ouverts avant que la projection ne supprime la question
 * (et ses signalements, en cascade). {@link DisallowReplay} : un replay ne ré-émet aucune commande.
 */
@Component
@Order(100)
@DisallowReplay
@ProcessingGroup("theme-projection")
public class TopicAvailabilityPolicy {

    private final QuestionRepositoryPort questions;
    private final CommandGateway commands;
    private final QuestionReportRepositoryPort reports;
    private final TopicRepositoryPort topics;

    public TopicAvailabilityPolicy(
            QuestionRepositoryPort questions,
            CommandGateway commands,
            QuestionReportRepositoryPort reports,
            TopicRepositoryPort topics) {
        this.questions = questions;
        this.commands = commands;
        this.reports = reports;
        this.topics = topics;
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
        check(event.topicId(), event.questionId());
    }

    @EventHandler
    public void on(QuestionEvent.QuestionDeletedEvent event) {
        check(event.topicId(), event.questionId());
        for (var report : reports.findOpenByQuestionId(event.questionId())) {
            commands.send(
                    new QuestionReportCommand.ResolveQuestionReportCommand(
                            report.threadId(),
                            report.reportId(),
                            QuestionReportResolution.RESOLVED,
                            event.requestedBy(),
                            "QUESTION_DELETED",
                            event.deletedAt()));
        }
    }

    private void checkQuestion(String questionId) {
        questions.findById(questionId).ifPresent(question -> check(question.topicId(), questionId));
    }

    /** Seul un sujet publié peut repasser en brouillon (un brouillon supprimé n'existe plus). */
    private void check(String topicId, String leavingQuestionId) {
        boolean published = topics.findById(topicId).map(topic -> topic.status() == TopicStatus.PUBLISHED).orElse(false);
        if (published
                && questions.countPlayableByTopicIdExcluding(topicId, leavingQuestionId)
                < TopicRules.MIN_QUESTIONS_TO_PUBLISH) {
            commands.send(new TopicCommand.DraftTopicCommand(topicId, "INSUFFICIENT_QUESTIONS"));
        }
    }
}

package com.joutio.theme.application.policy;

import com.joutio.microservice.core.domain.constant.JoutioConstants;
import com.joutio.theme.domain.command.QuestionCommand;
import com.joutio.theme.domain.event.QuestionReportEvent;
import com.joutio.theme.domain.model.Question;
import com.joutio.theme.domain.model.QuestionAutoSuspension;
import com.joutio.theme.domain.model.QuestionReportRules;
import com.joutio.theme.domain.model.QuestionStatus;
import com.joutio.theme.domain.model.QuestionSuspensionReason;
import com.joutio.theme.domain.port.out.QuestionReportRepositoryPort;
import com.joutio.theme.domain.port.out.QuestionRepositoryPort;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.DisallowReplay;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Politique de suspension automatique d'une question signalée.
 *
 * <p>Au {@code threshold}-ième signaleur distinct dans la fenêtre, la question approuvée est suspendue.
 * Les signalements clos sans modification comptent toujours (voir {@link QuestionAutoSuspension}).
 * Le comptage ne remonte jamais avant la dernière réactivation/restauration.
 *
 * <p>Politique, pas projection : {@link DisallowReplay} empêche un replay de {@code theme-projection}
 * de ré-émettre des commandes. Le signaleur de l'événement est compté à part, le résultat ne dépend
 * donc pas de l'ordre d'exécution avec {@code QuestionReportProjection}. Toutes les valeurs
 * temporelles viennent de l'événement ; l'agrégat reste le garde-fou (statut, dernière réactivation).
 */
@Component
@DisallowReplay
@ProcessingGroup("theme-projection")
public class QuestionReportSuspensionPolicy {

    private static final Logger logger = LoggerFactory.getLogger(QuestionReportSuspensionPolicy.class);

    private final QuestionReportRepositoryPort reports;
    private final QuestionRepositoryPort questions;
    private final CommandGateway commandGateway;
    private final QuestionAutoSuspension settings;

    public QuestionReportSuspensionPolicy(
            QuestionReportRepositoryPort reports,
            QuestionRepositoryPort questions,
            CommandGateway commandGateway,
            QuestionAutoSuspension settings) {
        this.reports = reports;
        this.questions = questions;
        this.commandGateway = commandGateway;
        this.settings = settings;
    }

    @EventHandler
    public void on(QuestionReportEvent.QuestionReportedEvent event) {
        if (!settings.enabled()) {
            return;
        }
        Question question = questions.findById(event.questionId()).orElse(null);
        if (question == null || question.status() != QuestionStatus.APPROVED) {
            return;
        }
        Instant since =
                QuestionReportRules.windowStart(
                        event.reportedAt(),
                        settings.window(),
                        question.reinstatedAt(),
                        question.restoredAt());
        int distinctReporters =
                reports.countDistinctUnresolvedReportersSince(
                                event.questionId(), since, event.reporterId())
                        + 1;
        if (!QuestionReportRules.shouldSuspend(distinctReporters, settings.threshold())) {
            return;
        }
        String note =
                distinctReporters + " signalements distincts en " + settings.windowDays() + " jour(s)";
        commandGateway.send(
                new QuestionCommand.SuspendQuestionCommand(
                        event.questionId(),
                        QuestionSuspensionReason.AUTO_REPORT_THRESHOLD,
                        note,
                        JoutioConstants.SYSTEM_USER_ID,
                        event.reportId(),
                        event.reportedAt(),
                        distinctReporters,
                        (int) settings.windowDays(),
                        event.reportedAt()));
        logger.info("Question {} suspendue automatiquement : {}", event.questionId(), note);
    }
}

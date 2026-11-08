package com.joutio.theme.application.projection;

import com.joutio.microservice.core.domain.model.i18n.Language;
import com.joutio.theme.domain.event.QuestionEvent;
import com.joutio.theme.domain.model.Question;
import com.joutio.theme.domain.model.QuestionContent;
import com.joutio.theme.domain.model.QuestionStatus;
import com.joutio.theme.domain.port.out.QuestionRepositoryPort;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.UnaryOperator;

@Component
@org.springframework.core.annotation.Order(0)
@ProcessingGroup("theme-projection")
public class QuestionProjection {

    private final QuestionRepositoryPort questionRepositoryPort;

    public QuestionProjection(QuestionRepositoryPort questionRepositoryPort) {
        this.questionRepositoryPort = questionRepositoryPort;
    }

    @EventHandler
    public void on(QuestionEvent.QuestionCreatedEvent event) {
        Question question =
                Question.builder()
                        .questionId(event.questionId())
                        .topicId(event.topicId())
                        .contents(new EnumMap<>(event.contents()))
                        .imageUrl(event.imageUrl())
                        .correctAnswer(event.correctAnswer())
                        .status(QuestionStatus.PENDING)
                        .difficulty(null)
                        .creatorId(event.creatorId())
                        .updatedBy(event.creatorId())
                        .createdAt(event.createdAt())
                        .updatedAt(event.createdAt())
                        .build();
        questionRepositoryPort.save(question);
    }

    @EventHandler
    public void on(QuestionEvent.QuestionTranslationsAddedEvent event) {
        questionRepositoryPort
                .findById(event.questionId())
                .ifPresent(
                        question -> {
                            Map<Language, QuestionContent> contents =
                                    new EnumMap<>(question.contents());
                            contents.putAll(event.contents());

                            questionRepositoryPort.save(
                                    question.toBuilder()
                                            .contents(contents)
                                            .updatedBy(event.updatedBy())
                                            .updatedAt(event.updatedAt())
                                            .build());
                        });
    }

    @EventHandler
    public void on(QuestionEvent.QuestionTranslationAddedEvent event) {
        questionRepositoryPort
                .findById(event.questionId())
                .ifPresent(
                        question -> {
                            Map<Language, QuestionContent> contents =
                                    new EnumMap<>(question.contents());
                            contents.put(
                                    event.language(),
                                    new QuestionContent(
                                            event.language(), event.text(), event.answers()));

                            questionRepositoryPort.save(
                                    question.toBuilder()
                                            .contents(contents)
                                            .updatedBy(event.updatedBy())
                                            .updatedAt(event.updatedAt())
                                            .build());
                        });
    }

    @EventHandler
    public void on(QuestionEvent.QuestionTextUpdatedEvent event) {
        updateContent(
                event.questionId(),
                event.language(),
                event.updatedBy(),
                event.updatedAt(),
                content ->
                        new QuestionContent(content.language(), event.text(), content.answers()));
    }

    @EventHandler
    public void on(QuestionEvent.QuestionAnswersUpdatedEvent event) {
        updateContent(
                event.questionId(),
                event.language(),
                event.updatedBy(),
                event.updatedAt(),
                content ->
                        new QuestionContent(content.language(), content.text(), event.answers()));
    }

    @EventHandler
    public void on(QuestionEvent.QuestionCorrectAnswerUpdatedEvent event) {
        questionRepositoryPort
                .findById(event.questionId())
                .ifPresent(
                        question ->
                                questionRepositoryPort.save(
                                        question.toBuilder()
                                                .correctAnswer(event.correctAnswer())
                                                .updatedBy(event.updatedBy())
                                                .updatedAt(event.updatedAt())
                                                .build()));
    }

    @EventHandler
    public void on(QuestionEvent.QuestionImageUrlUpdatedEvent event) {
        questionRepositoryPort
                .findById(event.questionId())
                .ifPresent(
                        question ->
                                questionRepositoryPort.save(
                                        question.toBuilder()
                                                .imageUrl(event.imageUrl())
                                                .updatedBy(event.updatedBy())
                                                .updatedAt(event.updatedAt())
                                                .build()));
    }

    @EventHandler
    public void on(QuestionEvent.QuestionApprovedEvent event) {
        questionRepositoryPort
                .findById(event.questionId())
                .ifPresent(
                        question ->
                                questionRepositoryPort.save(
                                        question.toBuilder()
                                                .status(QuestionStatus.APPROVED)
                                                .reinstatedAt(
                                                        question.status()
                                                                        == QuestionStatus.SUSPENDED
                                                                ? event.approvedAt()
                                                                : question.reinstatedAt())
                                                .updatedBy(event.updatedBy())
                                                .updatedAt(event.approvedAt())
                                                .build()));
    }

    @EventHandler
    public void on(QuestionEvent.QuestionRejectedEvent event) {
        questionRepositoryPort
                .findById(event.questionId())
                .ifPresent(
                        question ->
                                questionRepositoryPort.save(
                                        question.toBuilder()
                                                .status(QuestionStatus.REJECTED)
                                                .rejectionReason(event.reason())
                                                .updatedBy(event.updatedBy())
                                                .updatedAt(event.rejectedAt())
                                                .build()));
    }

    @EventHandler
    public void on(QuestionEvent.QuestionDifficultyUpdatedEvent event) {
        questionRepositoryPort
                .findById(event.questionId())
                .ifPresent(
                        question ->
                                questionRepositoryPort.save(
                                        question.toBuilder()
                                                .difficulty(event.difficulty())
                                                .updatedAt(event.updatedAt())
                                                .build()));
    }

    @EventHandler
    public void on(QuestionEvent.QuestionSuspendedEvent event) {
        questionRepositoryPort
                .findById(event.questionId())
                .ifPresent(
                        question ->
                                questionRepositoryPort.save(
                                        question.toBuilder()
                                                .status(QuestionStatus.SUSPENDED)
                                                .suspensionReason(event.reason())
                                                .suspensionNote(event.note())
                                                .suspendedAt(event.suspendedAt())
                                                .suspendedBy(event.requestedBy())
                                                .build()));
    }

    @EventHandler
    public void on(QuestionEvent.QuestionReinstatedEvent event) {
        questionRepositoryPort
                .findById(event.questionId())
                .ifPresent(
                        question ->
                                questionRepositoryPort.save(
                                        question.toBuilder()
                                                .status(QuestionStatus.APPROVED)
                                                .reinstatedAt(event.reinstatedAt())
                                                .build()));
    }

    @EventHandler
    public void on(QuestionEvent.QuestionArchivedEvent event) {
        questionRepositoryPort
                .findById(event.questionId())
                .ifPresent(
                        question ->
                                questionRepositoryPort.save(
                                        question.toBuilder()
                                                .status(QuestionStatus.ARCHIVED)
                                                .archiveNote(event.note())
                                                .archivedAt(event.archivedAt())
                                                .archivedBy(event.requestedBy())
                                                .build()));
    }

    @EventHandler
    public void on(QuestionEvent.QuestionRestoredEvent event) {
        questionRepositoryPort
                .findById(event.questionId())
                .ifPresent(
                        question ->
                                questionRepositoryPort.save(
                                        question.toBuilder()
                                                .status(QuestionStatus.PENDING)
                                                .restoredAt(event.restoredAt())
                                                .build()));
    }

    private void updateContent(
            String questionId,
            Language language,
            String updatedBy,
            Instant updatedAt,
            UnaryOperator<QuestionContent> change) {
        questionRepositoryPort
                .findById(questionId)
                .ifPresent(
                        question -> {
                            QuestionContent content = question.contents().get(language);
                            if (content == null) {
                                return;
                            }
                            Map<Language, QuestionContent> contents =
                                    new EnumMap<>(question.contents());
                            contents.put(language, change.apply(content));

                            questionRepositoryPort.save(
                                    question.toBuilder()
                                            .contents(contents)
                                            .updatedBy(updatedBy)
                                            .updatedAt(updatedAt)
                                            .build());
                        });
    }

    @EventHandler
    public void on(QuestionEvent.QuestionResubmittedEvent event) {
        questionRepositoryPort
                .findById(event.questionId())
                .ifPresent(
                        q ->
                                questionRepositoryPort.save(
                                        q.toBuilder()
                                                .status(QuestionStatus.PENDING)
                                                .rejectionReason(null)
                                                .updatedBy(event.requestedBy())
                                                .updatedAt(event.occurredAt())
                                                .build()));
    }

    @EventHandler
    public void on(QuestionEvent.QuestionDeletedEvent event) {
        questionRepositoryPort.deleteById(event.questionId());
    }
}

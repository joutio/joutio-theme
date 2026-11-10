package com.joutio.theme.application.projection;

import com.joutio.theme.domain.event.QuestionReportEvent;
import com.joutio.theme.domain.model.QuestionReport;
import com.joutio.theme.domain.model.QuestionReportStatus;
import com.joutio.theme.domain.port.out.QuestionReportRepositoryPort;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Projection des signalements. La suspension automatique vit dans
 * {@code QuestionReportSuspensionPolicy}, qui n'est pas rejouée.
 */
@Component
@ProcessingGroup("theme-projection")
public class QuestionReportProjection {

    private final QuestionReportRepositoryPort reportRepository;

    public QuestionReportProjection(QuestionReportRepositoryPort reportRepository) {
        this.reportRepository = reportRepository;
    }

    @EventHandler
    @Transactional
    public void on(QuestionReportEvent.QuestionReportedEvent event) {
        reportRepository.save(
                QuestionReport.builder()
                        .reportId(event.reportId())
                        .threadId(event.threadId())
                        .questionId(event.questionId())
                        .topicId(event.topicId())
                        .reporterId(event.reporterId())
                        .gameId(event.gameId())
                        .reasons(event.reasons())
                        .comment(event.comment())
                        .language(event.language())
                        .snapshot(event.snapshot())
                        .status(QuestionReportStatus.OPEN)
                        .createdAt(event.reportedAt())
                        .build());
    }

    @EventHandler
    @Transactional
    public void on(QuestionReportEvent.QuestionReportReviewedEvent event) {
        reportRepository
                .findById(event.reportId())
                .ifPresent(
                        report ->
                                reportRepository.save(
                                        report.toBuilder()
                                                .status(event.status())
                                                .reviewedBy(event.reviewerId())
                                                .reviewedAt(event.reviewedAt())
                                                .resolutionNote(event.note())
                                                .build()));
    }
}

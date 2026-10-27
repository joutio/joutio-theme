package com.joutio.theme.domain.event;

import com.joutio.microservice.core.domain.model.i18n.Language;
import com.joutio.theme.domain.model.QuestionReportReason;
import com.joutio.theme.domain.model.QuestionReportSnapshot;
import com.joutio.theme.domain.model.QuestionReportStatus;

import java.time.Instant;
import java.util.Set;

public interface QuestionReportEvent {

    String threadId();

    record QuestionReportedEvent(
            String threadId,
            String reportId,
            String questionId,
            String topicId,
            String topicCreatorId,
            String reporterId,
            String gameId,
            Set<QuestionReportReason> reasons,
            String comment,
            Language language,
            QuestionReportSnapshot snapshot,
            Instant reportedAt
    ) implements QuestionReportEvent {
    }

    record QuestionReportReviewedEvent(
            String threadId,
            String reportId,
            QuestionReportStatus status,
            String reviewerId,
            String note,
            Instant reviewedAt
    ) implements QuestionReportEvent {
    }
}

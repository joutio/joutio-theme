package com.joutio.theme.domain.model;

/** Décision de clôture d'un signalement. */
public enum QuestionReportResolution {
    RESOLVED,
    DISMISSED;

    public QuestionReportStatus status() {
        return this == RESOLVED ? QuestionReportStatus.RESOLVED : QuestionReportStatus.DISMISSED;
    }
}

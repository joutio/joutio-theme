package com.joutio.theme.domain.query;

import com.joutio.theme.domain.model.QuestionReportSort;
import com.joutio.theme.domain.model.QuestionReportSummaryFilter;

import java.util.List;

public interface QuestionReportQuery {

    record GetQuestionReportByIdQuery(String reportId) {
    }

    record GetQuestionReportsByQuestionQuery(String questionId) {
    }

    /** Compteurs batch pour une page de questions (catalogue admin / atelier). */
    record GetQuestionReportCountsQuery(List<String> questionIds) {
    }

    record GetReportedQuestionPageQuery(
            QuestionReportSummaryFilter filter,
            QuestionReportSort sort,
            int page,
            int size
    ) {
    }

    record GetQuestionReportStatsQuery() {
    }
}

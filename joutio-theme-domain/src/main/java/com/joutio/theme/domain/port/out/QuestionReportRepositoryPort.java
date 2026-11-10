package com.joutio.theme.domain.port.out;

import com.joutio.theme.domain.model.QuestionReport;
import com.joutio.theme.domain.model.QuestionReportCount;
import com.joutio.theme.domain.model.QuestionReportSort;
import com.joutio.theme.domain.model.QuestionReportStatus;
import com.joutio.theme.domain.model.QuestionReportSummaryFilter;
import com.joutio.theme.domain.model.QuestionReportSummaryPage;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface QuestionReportRepositoryPort {

    void save(QuestionReport report);

    Optional<QuestionReport> findById(String reportId);

    List<QuestionReport> findByQuestionId(String questionId);

    List<QuestionReport> findOpenByQuestionId(String questionId);

    List<QuestionReportCount> findCountsByQuestionIds(List<String> questionIds);

    QuestionReportSummaryPage findSummaryPage(QuestionReportSummaryFilter filter,
                                              QuestionReportSort sort,
                                              int page,
                                              int size);

    /**
     * Signaleurs distincts d'une question depuis {@code since}, signalements ouverts ou clos sans
     * modification, hors {@code excludedReporterId} (le signaleur de l'événement en cours, compté à part
     * pour ne pas dépendre de l'ordre des handlers).
     */
    int countDistinctUnresolvedReportersSince(String questionId, Instant since, String excludedReporterId);

    long countByStatus(QuestionReportStatus status);

    long countDistinctQuestionIdByStatus(QuestionReportStatus status);
}

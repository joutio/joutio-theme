package com.joutio.theme.domain.port.out;

import com.joutio.theme.domain.model.QuestionAnswerStats;

import java.util.Optional;

public interface QuestionAnswerStatsRepositoryPort {

    Optional<QuestionAnswerStats> findById(String questionId);

    void save(QuestionAnswerStats stats);
}

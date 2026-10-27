package com.joutio.theme.infrastructure.out.persistence.adapter;

import com.joutio.theme.domain.model.QuestionAnswerStats;
import com.joutio.theme.domain.port.out.QuestionAnswerStatsRepositoryPort;
import com.joutio.theme.infrastructure.out.persistence.mapper.QuestionAnswerStatsEntityMapper;
import com.joutio.theme.infrastructure.out.persistence.repository.QuestionAnswerStatsJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class QuestionAnswerStatsRepositoryAdapter implements QuestionAnswerStatsRepositoryPort {

    private final QuestionAnswerStatsJpaRepository repository;

    public QuestionAnswerStatsRepositoryAdapter(QuestionAnswerStatsJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<QuestionAnswerStats> findById(String questionId) {
        return repository.findById(questionId).map(QuestionAnswerStatsEntityMapper::toDomain);
    }

    @Override
    @Transactional
    public void save(QuestionAnswerStats stats) {
        repository.save(QuestionAnswerStatsEntityMapper.toEntity(stats));
    }
}

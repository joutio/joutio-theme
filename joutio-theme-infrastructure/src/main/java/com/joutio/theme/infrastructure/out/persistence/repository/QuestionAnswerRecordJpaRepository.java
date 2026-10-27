package com.joutio.theme.infrastructure.out.persistence.repository;

import com.joutio.theme.infrastructure.out.persistence.entity.QuestionAnswerRecordEntity;
import com.joutio.theme.infrastructure.out.persistence.entity.QuestionAnswerRecordId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionAnswerRecordJpaRepository
        extends JpaRepository<QuestionAnswerRecordEntity, QuestionAnswerRecordId> {
}

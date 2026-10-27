package com.joutio.theme.infrastructure.out.persistence.repository;

import com.joutio.theme.infrastructure.out.persistence.entity.QuestionAnswerStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionAnswerStatsJpaRepository extends JpaRepository<QuestionAnswerStatsEntity, String> {
}

package com.joutio.theme.infrastructure.out.persistence.adapter;

import com.joutio.theme.domain.port.out.TopicFollowerRefRepositoryPort;
import com.joutio.theme.infrastructure.out.persistence.entity.TopicFollowerRefEntity;
import com.joutio.theme.infrastructure.out.persistence.entity.TopicFollowerRefId;
import com.joutio.theme.infrastructure.out.persistence.repository.TopicFollowerRefJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TopicFollowerRefRepositoryAdapter implements TopicFollowerRefRepositoryPort {

    private final TopicFollowerRefJpaRepository repository;

    public TopicFollowerRefRepositoryAdapter(TopicFollowerRefJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void add(String topicId, String userId) {
        repository.save(new TopicFollowerRefEntity(new TopicFollowerRefId(topicId, userId)));
    }

    @Override
    @Transactional
    public void remove(String topicId, String userId) {
        TopicFollowerRefId id = new TopicFollowerRefId(topicId, userId);
        repository.findById(id).ifPresent(repository::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public int countByTopicId(String topicId) {
        return repository.countByTopicId(topicId);
    }
}

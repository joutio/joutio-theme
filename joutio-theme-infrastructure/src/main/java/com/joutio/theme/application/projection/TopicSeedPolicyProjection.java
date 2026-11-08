package com.joutio.theme.application.projection;

import com.joutio.theme.domain.event.QuestionEvent;
import com.joutio.theme.domain.event.TopicEvent;
import com.joutio.theme.domain.port.out.QuestionRepositoryPort;
import com.joutio.theme.domain.port.out.TopicSeedPolicyPort;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("theme-projection")
public class TopicSeedPolicyProjection {
    private final TopicSeedPolicyPort policy;
    private final QuestionRepositoryPort questions;

    public TopicSeedPolicyProjection(TopicSeedPolicyPort policy, QuestionRepositoryPort questions) {
        this.policy = policy;
        this.questions = questions;
    }

    @EventHandler
    public void on(TopicEvent.TopicDraftedEvent event) {
        policy.lock(event.topicId());
    }

    @EventHandler
    public void on(QuestionEvent.QuestionDeletedEvent event) {
        policy.lock(event.topicId());
    }

    @EventHandler
    public void on(QuestionEvent.QuestionArchivedEvent event) {
        lockQuestion(event.questionId());
    }

    @EventHandler
    public void on(QuestionEvent.QuestionSuspendedEvent event) {
        lockQuestion(event.questionId());
    }

    private void lockQuestion(String id) {
        questions.findById(id).ifPresent(q -> policy.lock(q.topicId()));
    }
}

package com.joutio.theme.application.policy;

import com.joutio.theme.domain.command.QuestionCommand;
import com.joutio.theme.domain.event.TopicEvent;
import com.joutio.theme.domain.model.Question;
import com.joutio.theme.domain.port.out.QuestionRepositoryPort;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.DisallowReplay;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Suppression d'un brouillon : chaque question du sujet est supprimée par sa propre commande, pour que
 * son flux d'événements se ferme aussi. {@code @Order(50)} lit les questions avant que la projection ne
 * supprime le sujet (et ses questions, en cascade). {@link DisallowReplay} : un replay ne ré-émet rien.
 */
@Component
@Order(50)
@DisallowReplay
@ProcessingGroup("theme-projection")
public class TopicDeletionPolicy {

    private final QuestionRepositoryPort questions;
    private final CommandGateway commands;

    public TopicDeletionPolicy(QuestionRepositoryPort questions, CommandGateway commands) {
        this.questions = questions;
        this.commands = commands;
    }

    @EventHandler
    public void on(TopicEvent.TopicDeletedEvent event) {
        for (Question question : questions.findByTopicId(event.topicId())) {
            commands.send(new QuestionCommand.DeleteQuestionCommand(question.questionId(), event.requestedBy()));
        }
    }
}

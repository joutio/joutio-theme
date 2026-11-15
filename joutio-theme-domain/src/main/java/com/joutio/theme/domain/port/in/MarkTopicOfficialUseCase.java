package com.joutio.theme.domain.port.in;

import com.joutio.theme.domain.command.TopicCommand;

import java.util.concurrent.CompletableFuture;

/** Badge « Officiel » d'un sujet (seed et administration). */
public interface MarkTopicOfficialUseCase {
    CompletableFuture<Object> setOfficial(TopicCommand.SetTopicOfficialCommand command);

    default void markOfficialAndWait(String topicId, String requestedBy) {
        setOfficial(new TopicCommand.SetTopicOfficialCommand(topicId, true, requestedBy)).join();
    }
}

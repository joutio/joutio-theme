package com.joutio.theme.domain.port.in;

import com.joutio.microservice.core.domain.model.i18n.Language;
import com.joutio.theme.domain.command.TopicCommand;
import com.joutio.theme.domain.model.TopicCategory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface CreateTopicUseCase {

    CompletableFuture<String> create(TopicCommand.CreateTopicCommand command);

    default CompletableFuture<String> create(
            String topicId,
            Map<Language, String> names,
            String description,
            TopicCategory category,
            String creatorId
    ) {
        return create(
                new TopicCommand.CreateTopicCommand(
                        topicId, names, description, category, null, null, null, creatorId));
    }

    default void createAndWait(
            String topicId,
            Map<Language, String> names,
            String description,
            TopicCategory category,
            String creatorId
    ) {
        create(topicId, names, description, category, creatorId).join();
    }

    /** Variante du seed : vignette, pictogramme et titres par palier et par langue (optionnels). */
    default void createAndWait(
            String topicId,
            Map<Language, String> names,
            String description,
            TopicCategory category,
            String imageUrl,
            String pictogramUrl,
            Map<Language, Map<String, String>> titles,
            String creatorId
    ) {
        create(new TopicCommand.CreateTopicCommand(
                topicId, names, description, category, imageUrl, pictogramUrl, titles, creatorId)).join();
    }
}

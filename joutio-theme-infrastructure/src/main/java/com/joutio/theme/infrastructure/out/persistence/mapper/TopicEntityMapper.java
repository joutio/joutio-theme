package com.joutio.theme.infrastructure.out.persistence.mapper;

import com.joutio.microservice.core.domain.model.i18n.Language;
import com.joutio.theme.domain.model.Topic;
import com.joutio.theme.domain.util.SearchText;
import com.joutio.theme.infrastructure.out.persistence.entity.TopicEntity;

import java.util.EnumMap;
import java.util.Map;

public final class TopicEntityMapper {

    private TopicEntityMapper() {
    }

    public static Topic toDomain(TopicEntity entity) {
        Map<Language, String> names = new EnumMap<>(Language.class);
        if (entity.getNameFr() != null) {
            names.put(Language.FR, entity.getNameFr());
        }
        if (entity.getNameEn() != null) {
            names.put(Language.EN, entity.getNameEn());
        }
        Map<Language, String> descriptions = new EnumMap<>(Language.class);
        if (entity.getDescription() != null) {
            descriptions.put(Language.FR, entity.getDescription());
        }
        if (entity.getDescriptionEn() != null) {
            descriptions.put(Language.EN, entity.getDescriptionEn());
        }
        return new Topic(
                entity.getTopicId(),
                names,
                entity.getDescription(),
                entity.getCategory(),
                entity.getStatus(),
                entity.getCreatorId(),
                entity.getUpdatedBy(),
                entity.getFollowersCounter(),
                entity.getGamesCounter(),
                entity.getQuestionsCounter(),
                entity.getImageUrl(),
                entity.getBannerUrl(),
                entity.getPictogramUrl(),
                TopicTitlesJson.read(entity.getTitleNames()),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                descriptions,
                entity.getQuestionsChangedAt()
        );
    }

    public static TopicEntity toEntity(Topic topic) {
        TopicEntity topicEntity = new TopicEntity();
        topicEntity.setTopicId(topic.topicId());
        Map<Language, String> names = topic.names() == null ? Map.of() : topic.names();
        String fr = names.get(Language.FR);
        String en = names.get(Language.EN);
        topicEntity.setNameFr(fr);
        topicEntity.setNameFrNormalized(SearchText.normalize(fr));
        topicEntity.setNameEn(en);
        topicEntity.setNameEnNormalized(SearchText.normalize(en));
        Map<Language, String> descriptions = topic.descriptions() == null ? Map.of() : topic.descriptions();
        topicEntity.setDescription(descriptions.getOrDefault(Language.FR, topic.description()));
        topicEntity.setDescriptionEn(descriptions.get(Language.EN));
        topicEntity.setCategory(topic.category());
        topicEntity.setStatus(topic.status());
        topicEntity.setCreatorId(topic.creatorId());
        topicEntity.setUpdatedBy(topic.updatedBy());
        topicEntity.setFollowersCounter(topic.followersCounter());
        topicEntity.setGamesCounter(topic.gamesCounter());
        topicEntity.setQuestionsCounter(topic.questionsCounter());
        topicEntity.setImageUrl(topic.imageUrl());
        topicEntity.setBannerUrl(topic.bannerUrl());
        topicEntity.setPictogramUrl(topic.pictogramUrl());
        topicEntity.setTitleNames(TopicTitlesJson.write(topic.titles()));
        topicEntity.setCreatedAt(topic.createdAt());
        topicEntity.setUpdatedAt(topic.updatedAt());
        topicEntity.setQuestionsChangedAt(topic.questionsChangedAt());
        return topicEntity;
    }
}

package com.joutio.theme.domain.model;

import com.joutio.microservice.core.domain.model.i18n.Language;
import lombok.Builder;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * Modèle domaine d'un thème multilingue : un nom par langue disponible ({@link #names()}),
 * sans langue source explicite. Le repli de lecture est déterministe, identique aux questions :
 * langue demandée, puis français, puis anglais, puis premier nom disponible.
 */
@Builder(toBuilder = true)
public record Topic(
        String topicId,
        Map<Language, String> names,
        String description,
        TopicCategory category,
        TopicStatus status,
        String creatorId,
        String updatedBy,
        Integer followersCounter,
        Integer gamesCounter,
        Map<QuestionStatus, Integer> questionsCounter,
        String imageUrl,
        String bannerUrl,
        /** Pictogramme carré de la tuile du catalogue (`TopicTile`), ou `null` : repli sur la vignette. */
        String pictogramUrl,
        /** Titres personnalisés par palier et par langue (ex. « Maréchal d'Empire »), ou `null`. */
        Map<Language, Map<String, String>> titles,
        Instant createdAt,
        Instant updatedAt,
        /**
         * Description par langue (2026-11-13), ou vide. {@link #description()} reste la description française
         * (contrat antérieur, repli des consommateurs qui ne lisent pas encore cette carte).
         */
        Map<Language, String> descriptions
) {

    /** Description dans {@code language}, sinon la française, sinon {@code null}. */
    public String descriptionIn(Language language) {
        if (descriptions != null && language != null && descriptions.get(language) != null) {
            return descriptions.get(language);
        }
        return descriptions != null && descriptions.get(Language.FR) != null ? descriptions.get(Language.FR) : description;
    }

    /** Langues pour lesquelles un nom est disponible. */
    public Set<Language> availableLanguages() {
        return names == null ? Set.of() : Set.copyOf(names.keySet());
    }

    /** Nom dans la langue demandée, avec repli déterministe (FR, puis EN, puis premier). */
    public String name(Language language) {
        if (names == null || names.isEmpty()) {
            return null;
        }
        String value = names.get(language);
        if (value != null) {
            return value;
        }
        value = names.get(Language.FR);
        if (value != null) {
            return value;
        }
        value = names.get(Language.EN);
        if (value != null) {
            return value;
        }
        return names.values().iterator().next();
    }

    /** Nom de repli (FR prioritaire), utilisé par les vues historiques et le tri interne. */
    public String name() {
        return name(Language.FR);
    }
}

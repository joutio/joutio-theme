package com.joutio.theme.infrastructure.config.seed;

import com.joutio.microservice.core.domain.model.i18n.Language;
import com.joutio.theme.domain.model.TopicCategory;

import java.util.List;
import java.util.Map;

/**
 * Définition validée d'un thème issu d'un fichier de seed YAML.
 */
public record TopicSeedDefinition(
        String topicId,
        Map<Language, String> names,
        String description,
        TopicCategory category,
        String imageUrl,
        /** Titres personnalisés (palier → libellé, par langue), ou {@code null} (repli client). */
        Map<Language, Map<String, String>> titles,
        List<QuestionSeedDefinition> questions) {

    /** Nom d'affichage/robuste pour les logs (FR prioritaire). */
    public String displayName() {
        return names.getOrDefault(Language.FR, names.values().stream().findFirst().orElse(topicId));
    }
}

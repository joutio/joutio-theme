package com.joutio.theme.domain.model;

import com.joutio.theme.domain.exception.TopicProblems;

import com.joutio.microservice.core.domain.model.i18n.Language;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Règles des titres personnalisés (palier connu, libellé non vide et borné). */
class TopicTitlesTest {

    @Test
    void normalizesLabelsAndDropsEmptyLanguages() {
        Map<Language, Map<String, String>> raw = new LinkedHashMap<>();
        raw.put(Language.FR, Map.of("OR", "  Maréchal d'Empire  "));
        raw.put(Language.EN, Map.of());

        Map<Language, Map<String, String>> titles = TopicTitles.normalize("topic", raw);

        assertNotNull(titles);
        assertEquals(Set.of(Language.FR), titles.keySet());
        assertEquals("Maréchal d'Empire", titles.get(Language.FR).get("OR"));
    }

    @Test
    void returnsNullWhenNothingIsDefined() {
        assertNull(TopicTitles.normalize("topic", null));
        assertNull(TopicTitles.normalize("topic", Map.of()));
    }

    @Test
    void rejectsUnknownTier() {
        TopicProblems.InvalidTopicTitlesProblem error = assertThrows(TopicProblems.InvalidTopicTitlesProblem.class,
                () -> TopicTitles.normalize("topic", Map.of(Language.FR, Map.of("GURU", "Gourou"))));
        assertTrue(error.getMessage().contains("GURU"));
    }

    @Test
    void rejectsBlankLabel() {
        assertThrows(TopicProblems.InvalidTopicTitlesProblem.class,
                () -> TopicTitles.normalize("topic", Map.of(Language.FR, Map.of("PLATINE", "  "))));
    }

    @Test
    void rejectsTooLongLabel() {
        String label = "x".repeat(TopicTitles.MAX_LABEL_LENGTH + 1);
        TopicProblems.InvalidTopicTitlesProblem error = assertThrows(TopicProblems.InvalidTopicTitlesProblem.class,
                () -> TopicTitles.normalize("topic", Map.of(Language.FR, Map.of("PLATINE", label))));
        assertTrue(error.getMessage().contains("too long"));
    }
}

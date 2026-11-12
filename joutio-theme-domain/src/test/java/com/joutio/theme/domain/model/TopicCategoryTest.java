package com.joutio.theme.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Catégories alignées sur les 17 du design. */
class TopicCategoryTest {

    @Test
    void seventeenCategoriesLikeTheDesign() {
        assertEquals(17, TopicCategory.values().length);
        assertEquals("Culture générale", TopicCategory.GENERAL.label());
    }
}

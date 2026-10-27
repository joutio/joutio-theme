package com.joutio.theme.domain.model;

/**
 * Compteur de sujets publiés pour une catégorie (facette du catalogue).
 */
public record TopicFacetCount(TopicCategory category, long count) {
}

package com.joutio.theme.domain.model;

/**
 * Nombre d'utilisations d'une URL d'image dans le read model theme
 * (couverture et bannière de sujet, illustration de question).
 */
public record ImageReferenceCount(String imageUrl, long count) {
}

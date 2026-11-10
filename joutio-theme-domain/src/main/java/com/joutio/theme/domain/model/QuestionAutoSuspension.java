package com.joutio.theme.domain.model;

import java.time.Duration;

/**
 * Réglage de la suspension automatique d'une question signalée : au {@code threshold}-ième signaleur
 * distinct dans la fenêtre {@code window}, la question approuvée est retirée du tirage.
 *
 * <p>Comptent les signalements ouverts <b>et</b> clos sans modification ({@code DISMISSED}) : clore
 * un signalement ne doit pas permettre à l'équipe du sujet de neutraliser la règle. Un signalement
 * corrigé ({@code RESOLVED}) ne compte plus.
 */
public record QuestionAutoSuspension(boolean enabled, int threshold, Duration window) {

    public static final QuestionAutoSuspension DISABLED =
            new QuestionAutoSuspension(false, Integer.MAX_VALUE, Duration.ZERO);

    public long windowDays() {
        return Math.max(1, window.toDays());
    }
}

package com.joutio.theme.domain.model;

import com.joutio.microservice.core.domain.model.i18n.Language;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Règles pures des signalements de questions : identifiants déterministes, validation des
 * entrées et politique de suspension automatique.
 */
public interface QuestionReportRules {

    int MAX_COMMENT_LENGTH = 500;
    int MAX_NOTE_LENGTH = 500;

    /** Identifiant déterministe du thread (joueur × question) : sérialise les signalements. */
    static String threadId(String questionId, String reporterId) {
        String seed = Objects.requireNonNull(questionId, "questionId") + "|"
                + Objects.requireNonNull(reporterId, "reporterId");
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8)).toString();
    }

    /** Violation constatée du signalement ; l'appelant lève le problème de son contexte. */
    static Optional<String> reportViolation(Set<QuestionReportReason> reasons, String comment) {
        if (reasons == null || reasons.isEmpty()) {
            return Optional.of("At least one report reason is required");
        }
        if (comment != null && comment.length() > MAX_COMMENT_LENGTH) {
            return Optional.of("Report comment is too long");
        }
        return Optional.empty();
    }

    static Optional<String> noteViolation(String note) {
        return note != null && note.length() > MAX_NOTE_LENGTH
                ? Optional.of("Resolution note is too long")
                : Optional.empty();
    }

    /**
     * Début de la fenêtre de comptage : on ne compte pas les signalements antérieurs à la
     * dernière réactivation/restauration, sinon une question réactivée serait immédiatement
     * re-suspendue par d'anciens signalements encore ouverts.
     */
    static Instant windowStart(Instant now, Duration window, Instant reinstatedAt, Instant restoredAt) {
        Instant start = now.minus(window == null ? Duration.ofDays(7) : window);
        if (reinstatedAt != null && reinstatedAt.isAfter(start)) {
            start = reinstatedAt;
        }
        if (restoredAt != null && restoredAt.isAfter(start)) {
            start = restoredAt;
        }
        return start;
    }

    static boolean shouldSuspend(int distinctReporters, int threshold) {
        return threshold > 0 && distinctReporters >= threshold;
    }

    static Language languageOrNull(String language) {
        return language == null || language.isBlank() ? null : Language.fromCode(language);
    }
}

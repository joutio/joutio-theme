package com.joutio.theme.domain.model;

/** Motif d'un signalement de question déposé par un joueur. */
public enum QuestionReportReason {
    /** Énoncé ambigu, faute, question incompréhensible. */
    QUESTION,
    /** Image absente, erronée ou de mauvaise qualité. */
    IMAGE,
    /** Bonne réponse incorrecte ou réponses incohérentes. */
    ANSWERS,
    /**
     * Image ou contenu protégé : droit d'auteur, droit à l'image, imitation d'un sujet officiel (2026-11-15, LCEN/DSA).
     * Les consommateurs du contrat doivent monter leur version de {@code joutio-theme-domain} avant de le recevoir.
     */
    COPYRIGHT
}

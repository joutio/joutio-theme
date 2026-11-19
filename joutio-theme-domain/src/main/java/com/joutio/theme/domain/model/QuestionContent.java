package com.joutio.theme.domain.model;

import com.joutio.microservice.core.domain.model.i18n.Language;

import java.util.Map;

/**
 * Contenu d'une question dans une langue : texte, réponses A-D et description de l'image
 * ({@code imageAlt}, lue par les lecteurs d'écran ; {@code null} sans image ou non renseignée).
 * Le choix correct ({@link Question#correctAnswer()}) et l'image elle-même sont portés par la
 * question et partagés entre les langues.
 */
public record QuestionContent(
        Language language,
        String text,
        Map<QuestionChoice, String> answers,
        String imageAlt
) {

    public QuestionContent withText(String text) {
        return new QuestionContent(language, text, answers, imageAlt);
    }

    public QuestionContent withAnswers(Map<QuestionChoice, String> answers) {
        return new QuestionContent(language, text, answers, imageAlt);
    }

    public QuestionContent withImageAlt(String imageAlt) {
        return new QuestionContent(language, text, answers, imageAlt);
    }
}

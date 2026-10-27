package com.joutio.theme.infrastructure.config.seed;

import com.joutio.microservice.core.domain.model.i18n.Language;
import com.joutio.theme.domain.model.QuestionChoice;
import com.joutio.theme.domain.model.QuestionContent;
import com.joutio.theme.domain.model.QuestionDifficulty;

import java.util.Map;

/**
 * Définition validée d'une question issue d'un fichier de seed YAML :
 * un contenu par langue disponible (le français est toujours présent, l'anglais optionnel)
 * et une difficulté initiale optionnelle (remplacée par le calcul sur les réponses réelles
 * dès que l'échantillon est suffisant).
 */
public record QuestionSeedDefinition(
        Map<Language, QuestionContent> contents,
        QuestionChoice correctAnswer,
        String imageUrl,
        QuestionDifficulty difficulty) {
}

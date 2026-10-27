package com.joutio.theme.domain.port.in;

import com.joutio.theme.domain.command.QuestionCommand;

import java.util.concurrent.CompletableFuture;

/**
 * Port entrant — ajout (ou remplacement) de contenus localisés d'une question.
 */
public interface AddQuestionTranslationsUseCase {

    CompletableFuture<String> add(QuestionCommand.AddQuestionTranslationsCommand command);
}

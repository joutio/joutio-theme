package com.joutio.theme.application.service;

import com.joutio.theme.domain.command.QuestionCommand;
import com.joutio.theme.domain.port.in.AddQuestionTranslationsUseCase;
import com.joutio.theme.domain.port.in.ApproveQuestionUseCase;
import com.joutio.theme.domain.port.in.CreateQuestionUseCase;
import com.joutio.theme.domain.port.in.RejectQuestionUseCase;
import com.joutio.theme.domain.port.in.UpdateQuestionDifficultyUseCase;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class QuestionCommandService implements CreateQuestionUseCase, ApproveQuestionUseCase, RejectQuestionUseCase,
        AddQuestionTranslationsUseCase, UpdateQuestionDifficultyUseCase {

    private final CommandGateway commandGateway;

    public QuestionCommandService(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }


    @Override
    public CompletableFuture<String> create(QuestionCommand.CreateQuestionCommand command) {
        return commandGateway.send(command);
    }

    @Override
    public CompletableFuture<String> add(QuestionCommand.AddQuestionTranslationsCommand command) {
        return commandGateway.send(command);
    }

    @Override
    public CompletableFuture<String> approve(QuestionCommand.ApproveQuestionCommand command) {
        return commandGateway.send(command);
    }
    @Override
    public CompletableFuture<String> reject(QuestionCommand.RejectQuestionCommand command) {
        return commandGateway.send(command);
    }

    @Override
    public CompletableFuture<String> updateDifficulty(QuestionCommand.UpdateQuestionDifficultyCommand command) {
        return commandGateway.send(command);
    }
}

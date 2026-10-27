package com.joutio.theme.application.service;

import com.joutio.theme.domain.command.TopicCommand;
import com.joutio.theme.domain.port.in.CreateTopicUseCase;
import com.joutio.theme.domain.port.in.PublishTopicUseCase;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class TopicCommandService implements CreateTopicUseCase, PublishTopicUseCase {

    private final CommandGateway commandGateway;

    public TopicCommandService(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @Override
    public CompletableFuture<String> create(TopicCommand.CreateTopicCommand command) {
        return commandGateway.send(command);
    }

    @Override
    public CompletableFuture<String> publish(TopicCommand.PublishTopicCommand command) {
        return commandGateway.send(command);
    }

}


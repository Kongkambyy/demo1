package com.gilbert.demo.domain.usecases.message;

import com.gilbert.demo.data.repository.MessageRepository;
import com.gilbert.demo.domain.entities.Message;

public class SendMessageUseCase {

    private final MessageRepository messageRepository;

    public SendMessageUseCase(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    /* Sender en ny besked ved at gemme den i databasen*/
    public Message sendMessage(Message message) {
        return messageRepository.save(message);
    }
}

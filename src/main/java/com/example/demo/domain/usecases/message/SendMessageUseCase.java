package com.example.demo.domain.usecases.message;

import com.example.demo.data.repository.MessageRepository;
import com.example.demo.domain.entities.Message;

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

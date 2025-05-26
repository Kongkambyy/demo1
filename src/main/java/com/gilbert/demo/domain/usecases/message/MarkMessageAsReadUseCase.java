package com.gilbert.demo.domain.usecases.message;

import com.gilbert.demo.data.repository.MessageRepository;

public class MarkMessageAsReadUseCase {

    private final MessageRepository messageRepository;

    public MarkMessageAsReadUseCase(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    /*Marker én besked som læst*/
    public void markMessageAsRead(String messageId) {
        messageRepository.markAsRead(messageId);
    }

    /*marker alle beskeder i en samtale som læst for en bruger*/
    public void markConversationAsRead(String userId, String otherUserId) {
        messageRepository.markConversationAsRead(userId, otherUserId);
    }
}

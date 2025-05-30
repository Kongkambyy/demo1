package com.gilbert.demo.domain.usecases.message;

import com.gilbert.demo.data.repository.MessageRepository;
import com.gilbert.demo.domain.entities.Message;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetMessagesUseCase {

    private final MessageRepository messageRepository;

    public GetMessagesUseCase(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    /* hent alle beskeder, hvor en bruger er enten afsender eller modtager*/
    public List<Message> getMessagesByUser(String userId) {
        return messageRepository.findByUserId(userId);
    }

    /* Hent alle beskeder mellem to brugere(samtale)*/
    public List<Message> getConversation(String userId1, String userId2) {
        return messageRepository.findConversation(userId1, userId2);
    }

    /*hent den nyeste besked i hver samtale brugeren deltager i*/
    public List<Message> getLatestMessagesByUser(String userId) {
        return messageRepository.findLatestMessagesByUserId(userId);
    }

    /*tæl ulæste beskeder for en bruger*/
    public int countUnreadMessages(String userId) {
        return messageRepository.countUnreadMessages(userId);
    }
}

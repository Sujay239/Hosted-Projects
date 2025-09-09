package com.chatapp.services;

import com.chatapp.model.MessageEntity;
import com.chatapp.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MessageService {

    @Autowired
    private MessageRepository msgRepo;

    public void saveMsg(MessageEntity entity){
        msgRepo.save(entity);
    }

    public List<MessageEntity> getConversation(String userA, String userB) {
        return msgRepo.getConversation(userA, userB);
    }
}

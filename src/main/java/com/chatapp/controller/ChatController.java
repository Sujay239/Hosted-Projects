package com.chatapp.controller;

import com.chatapp.dto.ChatMessage;
import com.chatapp.model.MessageEntity;
import com.chatapp.model.RoomMessageEntity;
import com.chatapp.model.Users;
import com.chatapp.repository.RoomMessageRepository;
import com.chatapp.repository.UserRepository;
import com.chatapp.services.MessageService;
import com.chatapp.services.OnlineUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class ChatController {

    private final OnlineUserService onlineUserService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;


    @Autowired
    private RoomMessageRepository roomMessageRepository;

    public ChatController(OnlineUserService onlineUserService,
                          SimpMessagingTemplate messagingTemplate,
                          MessageService messageService,
                          UserRepository userRepository) {
        this.onlineUserService = onlineUserService;
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
        this.userRepository = userRepository;
    }

    // Public chat
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage message) {
        message.setTimestamp(Instant.now().toString());
        log.info("Received ChatMessage object: {}", message);

        // Log each field to see if deserialization worked
        log.info("sender: {}", message.getSender());
        log.info("receiver: {}", message.getReceiver());
        log.info("content: {}", message.getContent());
        log.info("type: {}", message.getType());
        log.info("timestamp: {}", message.getTimestamp());
        messagingTemplate.convertAndSend("/topic/public", message);
    }

    // Add user
    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage message) {
        onlineUserService.addUser(message.getSender());
        message.setTimestamp(Instant.now().toString());
        messagingTemplate.convertAndSend("/topic/public", message);
    }

    // Private chat
    @MessageMapping("/chat.privateMessage")
    public void sendPrivateMessage(@Payload ChatMessage message) {
      // safety check

//        log.info("Received ChatMessage object: {}", message);
//
//        // Log each field to see if deserialization worked
//        log.info("sender: {}", message.getSender());
//        log.info("receiver: {}", message.getReceiver());
//        log.info("content: {}", message.getContent());
//        log.info("type: {}", message.getType());
//        log.info("timestamp: {}", message.getTimestamp());
//        message.setTimestamp(Instant.now().toString());
        log.info(message.toString());
        log.info(message.getSender());
        log.info(message.getReceiver());
        log.info(message.getContent());
        log.info(message.getType());
        log.info(message.getTimestamp());

        String senderUsername = message.getSender();
        message.setSender(senderUsername);
        message.setTimestamp(Instant.now().toString());

        Users sender = userRepository.findUsersByUsername(senderUsername)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Users receiver = userRepository.findUsersByUsername(message.getReceiver())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        // Persist message
        MessageEntity entity = new MessageEntity();
        entity.setSender_id(sender.getId());
        entity.setReceiver_id(receiver.getId());
        entity.setSender(sender.getUsername());
        entity.setReceiver(receiver.getUsername());
        entity.setContent(message.getContent());
        entity.setType(message.getType());
        entity.setTimestamp(message.getTimestamp());
        messageService.saveMsg(entity);



        // Send to receiver
        messagingTemplate.convertAndSendToUser(
                receiver.getUsername(),
                "/queue/messages",
                message
        );


        messagingTemplate.convertAndSend("/topic/public", message);
    }

    // Conversation history
    @GetMapping("/api/messages/conversation/{userA}/{userB}")
    @ResponseBody
    public List<ChatMessage> getConversation(@PathVariable String userA, @PathVariable String userB) {
        List<MessageEntity> messages = messageService.getConversation(userA, userB);
        return messages.stream().map(m -> {
            ChatMessage cm = new ChatMessage();
            cm.setSender(m.getSender());
            cm.setReceiver(m.getReceiver());
            cm.setContent(m.getContent());
            cm.setType(m.getType());
            cm.setTimestamp(m.getTimestamp());
            return cm;
        }).collect(Collectors.toList());
    }

    // === Room Chat ===
    @MessageMapping("/chat.roomMessage")
    public void sendRoomMessage(@Payload ChatMessage message) {
        message.setTimestamp(Instant.now().toString());
        message.setType("ROOM");

        log.info("Room chat: {}", message);

        // Persist message in room-specific table
        RoomMessageEntity entity = new RoomMessageEntity();
        entity.setSender(message.getSender());
        entity.setRoomId(message.getRoomId());
        entity.setContent(message.getContent());
        entity.setType(message.getType());
        entity.setTimestamp(message.getTimestamp());
        roomMessageRepository.save(entity);

        // Broadcast to room subscribers
        messagingTemplate.convertAndSend("/topic/rooms/" + message.getRoomId(), message);
    }

    @GetMapping("/api/messages/room/{roomId}")
    @ResponseBody
    public List<ChatMessage> getRoomMessages(@PathVariable String roomId) {
        List<RoomMessageEntity> messages = roomMessageRepository.findByRoomId(roomId);
        return messages.stream().map(m -> {
            ChatMessage cm = new ChatMessage();
            cm.setSender(m.getSender());
            cm.setRoomId(m.getRoomId());
            cm.setContent(m.getContent());
            cm.setType(m.getType());
            cm.setTimestamp(m.getTimestamp());
            return cm;
        }).toList();
    }






    // Handle disconnect
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if (username != null) {
            onlineUserService.removeUser(username);

            ChatMessage leaveMessage = new ChatMessage();
            leaveMessage.setSender(username);
            leaveMessage.setType("LEAVE");
            leaveMessage.setTimestamp(Instant.now().toString());

            messagingTemplate.convertAndSend("/topic/public", leaveMessage);
        }
    }
}

package com.chatapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage {
    private String sender;
    private String receiver;
    private String content;
    private String type; // JOIN, LEAVE, CHAT, PRIVATE
    private String timestamp;

    private String roomId; // For room messages
}

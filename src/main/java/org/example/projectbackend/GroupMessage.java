package org.example.projectbackend;

import java.util.UUID;

public class GroupMessage {
    private UUID messageId;
    private UUID groupId;
    private UUID senderId;
    private String content;
    private MessageType type;
    private String timestamp;

    public enum MessageType {
        TEXT, IMAGE, SYSTEM
    }

    public GroupMessage() {}

    // Constructor
    public GroupMessage(UUID groupId, UUID senderId, String content, MessageType type) {
        this.messageId = UUID.randomUUID();
        this.groupId = groupId;
        this.senderId = senderId;
        this.content = content;
        this.type = type;
        this.timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
    }

    // Getters and Setters
    public UUID getMessageId() { return messageId; }
    public void setMessageId(UUID messageId) { this.messageId = messageId; }

    public UUID getGroupId() { return groupId; }
    public void setGroupId(UUID groupId) { this.groupId = groupId; }

    public UUID getSenderId() { return senderId; }
    public void setSenderId(UUID senderId) { this.senderId = senderId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
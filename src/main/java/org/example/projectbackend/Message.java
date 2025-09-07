package org.example.projectbackend;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Message {
    private UUID messageId;
    private UUID senderId;
    private UUID receiverId;
    private String content;     // متن پیام
    private String filePath;    // 📎 مسیر فایل یا عکس
    private String timestamp;
    private MessageType type;

    public enum MessageType {
        TEXT, FILE, IMAGE, SYSTEM
    }

    public Message() {
        this.messageId = UUID.randomUUID();
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public Message(UUID senderId, UUID receiverId, String content, MessageType type) {
        this();
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.type = type;
    }

    // 👇 Getter/Setter جدید برای filePath
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    // باقی Getter/Setter ها مثل قبل
    public UUID getMessageId() {
        return messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }

    public UUID getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(UUID receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return STR."Message{messageId=\{messageId}, senderId=\{senderId}, content='\{content}', filePath='\{filePath}', timestamp='\{timestamp}', type=\{type}}";
    }
}

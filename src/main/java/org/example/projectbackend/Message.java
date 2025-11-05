package org.example.projectbackend;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;


public class Message {
    private UUID messageId;
    private UUID senderId;
    private UUID receiverId;
    private String content;
    private String timestamp;
    private MessageType type;

    public enum MessageType {
        TEXT, FILE, IMAGE, SYSTEM, DELETE, EDIT // <-- این گزینه اضافه شود
    }

    public Message() {
        // سازنده بدون پارامتر برای Gson
        this.messageId = UUID.randomUUID();
        // <<-- مقداردهی پیش‌فرض timestamp از اینجا حذف شد
    }

    public Message(UUID senderId, UUID receiverId, String content, MessageType type) {
        this(); // سازنده پیش‌فرض را برای گرفتن messageId فراخوانی می‌کند
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.type = type;
        // <<-- این خط مهم اضافه شده است تا زمان پیام در لحظه ساخت ثبت شود
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    // Getters and Setters
    public UUID getMessageId() {
        return messageId;
    }

    public UUID getReceiverId() { return receiverId; }
    public void setReceiverId(UUID receiverId) { this.receiverId = receiverId; }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
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
        return STR."Message{messageId=\{messageId}, senderId=\{senderId}, content='\{content}', timestamp='\{timestamp}', type=\{type}}";
    }
}
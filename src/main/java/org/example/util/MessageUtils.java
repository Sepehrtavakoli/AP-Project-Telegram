package org.example.util;

import com.google.gson.Gson;
import org.example.projectbackend.Message;

public class MessageUtils {
    private static final Gson gson = new Gson();

    // بررسی اینکه آیا پیام JSON است یا خیر
    public static boolean isJsonMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }
        String trimmed = message.trim();
        return trimmed.startsWith("{") && trimmed.endsWith("}");
    }

    // پارس کردن پیام JSON به شیء Message
    public static Message parseJsonMessage(String jsonMessage) {
        try {
            return gson.fromJson(jsonMessage, Message.class);
        } catch (Exception e) {
            System.err.println("Error parsing JSON message: " + e.getMessage());
            return null;
        }
    }

    // ایجاد پیام JSON از شیء Message
    public static String toJsonMessage(Message message) {
        try {
            return gson.toJson(message);
        } catch (Exception e) {
            System.err.println("Error converting message to JSON: " + e.getMessage());
            return null;
        }
    }

    // ایجاد پیام سیستم (متنی ساده)
    public static String createSystemMessage(String content) {
        return content;
    }

    // بررسی اینکه آیا پیام یک پیام سیستم است
    public static boolean isSystemMessage(String message) {
        if (message == null) return false;
        return message.contains("joined the chat") ||
                message.contains("left the chat") ||
                message.contains("Welcome to") ||
                message.contains("status\":\"sent\"");
    }
}
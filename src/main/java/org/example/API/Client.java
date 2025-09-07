package org.example.API;

import com.google.gson.Gson;
import javafx.application.Platform;
import org.example.projectbackend.GroupMessage;
import org.example.projectbackend.Message;
import org.example.model.User;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class Client {

    private Socket socket;
    private BufferedReader br;
    private PrintWriter pw;
    private volatile boolean isConnected; // 'volatile' is good practice for shared flags in multi-threaded environments
    private User user;
    private static final Gson gson = new Gson();

    // Use a thread-safe list to manage multiple listeners (e.g., ChatController, HomePageController)
    private final List<MessageListener> listeners = new CopyOnWriteArrayList<>();

    public interface MessageListener {
        // <<-- ورودی متد از String به Message تغییر می‌کند
        void onMessageReceived(Message message);
    }

    // Method to add a listener
    public void addMessageListener(MessageListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    // Method to remove a listener
    public void removeMessageListener(MessageListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public boolean connectToServer(String serverHost, int serverPort, User user) {
        if (isConnected) {
            System.out.println("Already connected.");
            return true;
        }
        this.user = user;
        System.out.println("Connecting as: " + user.getUserName());

        try {
            socket = new Socket(serverHost, serverPort);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw = new PrintWriter(socket.getOutputStream(), true);

            // Send User object as JSON to the server for identification
            String userJson = gson.toJson(user);
            pw.println(userJson);
            pw.flush();

            isConnected = true;
            startMessageListener(); // Start listening for messages only after a successful connection

            System.out.println("Connected to server as: " + user.getUserName());
            return true;
        } catch (IOException e) {
            System.err.println("Failed to connect: " + e.getMessage());
            isConnected = false; // Ensure status is correct on failure
            return false;
        }
    }

    // در Client.java این متدها رو اضافه کنید:
    public void sendGroupMessage(GroupMessage groupMessage) {
        if (isConnected && groupMessage != null) {
            String jsonMessage = gson.toJson(groupMessage);
            pw.println("GROUP_MSG:" + jsonMessage); // اضافه کردن prefix برای شناسایی
            pw.flush();
        }
    }

    public void sendGroupMessage(String content, UUID groupId) {
        if (isConnected && content != null && !content.trim().isEmpty()) {
            GroupMessage groupMsg = new GroupMessage(groupId, user.getUserId(), content, GroupMessage.MessageType.TEXT);
            sendGroupMessage(groupMsg);
        }
    }

    // متد startMessageListener رو به‌روزرسانی کنید:
    private void startMessageListener() {
        Thread listenerThread = new Thread(() -> {
            try {
                String receivedMsg;
                while (isConnected && (receivedMsg = br.readLine()) != null) {
                    try {
                        // تشخیص نوع پیام (خصوصی یا گروهی)
                        if (receivedMsg.startsWith("GROUP_MSG:")) {
                            // پیام گروهی
                            String jsonMsg = receivedMsg.substring(10); // حذف prefix
                            GroupMessage groupMessage = gson.fromJson(jsonMsg, GroupMessage.class);
                            if (groupMessage != null) {
                                Platform.runLater(() -> {
                                    for (MessageListener listener : listeners) {
                                        // تبدیل GroupMessage به Message برای سازگاری
                                        Message regularMessage = convertToRegularMessage(groupMessage);
                                        listener.onMessageReceived(regularMessage);
                                    }
                                });
                            }
                        } else {
                            // پیام خصوصی
                            Message message = gson.fromJson(receivedMsg, Message.class);
                            if (message != null) {
                                Platform.runLater(() -> {
                                    for (MessageListener listener : listeners) {
                                        listener.onMessageReceived(message);
                                    }
                                });
                            }
                        }
                    } catch (com.google.gson.JsonSyntaxException e) {
                        System.err.println("Received malformed JSON: " + receivedMsg);
                    }
                }
            } catch (IOException e) {
                if (isConnected) {
                    System.err.println("Connection lost: " + e.getMessage());
                }
            } finally {
                closeEverything();
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    // متد کمکی برای تبدیل GroupMessage به Message
    private Message convertToRegularMessage(GroupMessage groupMessage) {
        Message message = new Message();
        message.setMessageId(groupMessage.getMessageId());
        message.setSenderId(groupMessage.getSenderId());
        message.setContent("[GROUP] " + groupMessage.getContent());
        message.setType(Message.MessageType.valueOf(groupMessage.getType().name()));
        message.setTimestamp(groupMessage.getTimestamp());
        return message;
    }

    // Add this new, more versatile sendMessage method.
    public void sendMessage(Message message) {
        if (isConnected && message != null) {
            String jsonMessage = gson.toJson(message);
            pw.println(jsonMessage);
            pw.flush();
        }
    }

    // Replace the old sendMessage method with this updated version.
    public void sendMessage(String content, UUID receiverId) {
        if (isConnected && content != null && !content.trim().isEmpty()) {
            // This method now creates a text message and uses the new method above to send it.
            Message msgObj = new Message(user.getUserId(), receiverId, content, Message.MessageType.TEXT);
            sendMessage(msgObj);
        }
    }

    public void closeEverything() {
        if (!isConnected) return; // Prevent multiple closing attempts

        isConnected = false;
        try {
            if (socket != null) socket.close(); // Closing the socket will close its streams
            if (br != null) br.close();
            if (pw != null) pw.close();
            System.out.println("Client disconnected cleanly.");
        } catch (IOException e) {
            System.err.println("Error during client shutdown: " + e.getMessage());
        }
    }

    // Public getter to check connection status from other classes
    public boolean isConnected() {
        return isConnected;
    }


}
package org.example.API;

import com.google.gson.Gson;
import org.example.database.DatabaseHelper;
import org.example.projectbackend.GroupMessage;
import org.example.projectbackend.Message;
import org.example.model.User;  // تغییر به model

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final BufferedReader br;
    private final PrintWriter pw;
    private volatile boolean isConnected;
    private final User user;
    private static final Gson gson = new Gson();

    public ClientHandler(Socket socket) throws IOException {
        this.clientSocket = socket;
        this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.pw = new PrintWriter(socket.getOutputStream(), true);
        this.isConnected = true;

        String userJson = br.readLine();
        this.user = gson.fromJson(userJson, User.class);

        Server.onlineUsers.put(user.getUserId(), user.getUserName());
        System.out.println("Client connected: " + user.getUserName() + " - UUID: " + user.getUserId());

        // <<-- پیام‌های سیستمی را هم به صورت JSON ارسال می‌کنیم
        Message welcomeMsg = new Message(null, user.getUserId(), "Welcome to the server, " + user.getUserName() + "!", Message.MessageType.SYSTEM);
        sendMessage(gson.toJson(welcomeMsg));

        Message joinMsg = new Message(null, null, user.getUserName() + " joined the chat!", Message.MessageType.SYSTEM);
        Server.broadcastMessage(gson.toJson(joinMsg), this);
    }

    @Override
    public void run() {
        try {
            String msg;
            while (isConnected && (msg = br.readLine()) != null) {
                handleMessage(msg);
            }
        } catch (IOException e) {
            System.err.println("Error in handler: " + e.getMessage());
        } finally {
            closeEverything();
        }
    }

// فقط متد handleMessage را جایگزین کنید

    // در ClientHandler.java متد handleMessage رو به‌روزرسانی کنید:
    private void handleMessage(String receivedMsg) {
        try {
            if (receivedMsg.startsWith("GROUP_MSG:")) {
                // پردازش پیام گروهی
                String jsonMsg = receivedMsg.substring(10);
                GroupMessage groupMessage = gson.fromJson(jsonMsg, GroupMessage.class);

                // ذخیره در دیتابیس
                DatabaseHelper.saveGroupMessage(groupMessage);

                // ارسال به تمام اعضای گروه
                broadcastToGroupMembers(groupMessage);

            } else {
                // پردازش پیام خصوصی
                Message message = gson.fromJson(receivedMsg, Message.class);
                DatabaseHelper.savePrivateMessage(message);

                if (message.getReceiverId() != null) {
                    ClientHandler targetClient = findClientById(message.getReceiverId());
                    if (targetClient != null) {
                        targetClient.sendMessage(receivedMsg);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error handling message: " + receivedMsg + " | Error: " + e.getMessage());
        }
    }

    // متد جدید برای ارسال پیام به تمام اعضای گروه
    private void broadcastToGroupMembers(GroupMessage groupMessage) {
        // گرفتن لیست اعضای گروه از دیتابیس
        List<UUID> groupMembers = DatabaseHelper.getGroupMembers(groupMessage.getGroupId());

        for (UUID memberId : groupMembers) {
            // اگر عضو آنلاین باشد، پیام را ارسال کن
            ClientHandler memberClient = findClientById(memberId);
            if (memberClient != null && !memberId.equals(groupMessage.getSenderId())) {
                memberClient.sendMessage("GROUP_MSG:" + gson.toJson(groupMessage));
            }
        }

        System.out.println("Group message broadcasted to " + groupMembers.size() + " members");
    }

    // متد برای پیدا کردن کلاینت بر اساس userId
    private ClientHandler findClientById(UUID userId) {
        for (ClientHandler client : Server.clientHandlers) {
            if (client.getUser().getUserId().equals(userId)) {
                return client;
            }
        }
        return null;
    }

    public void sendMessage(String message) {
        if (isConnected) {
            pw.println(message);
            pw.flush();
        }
    }

    private String formatMessageForDisplay(Message message) {
        String senderName = Server.onlineUsers.getOrDefault(message.getSenderId(), "Unknown");
        return "[" + message.getTimestamp() + "] " + senderName + ": " + message.getContent();
    }

    public void closeEverything() {
        isConnected = false;

        // حذف کاربر از لیست آنلاین‌ها
        if (user != null) {
            Server.onlineUsers.remove(user.getUserId());
            Server.broadcastMessage(user.getUserName() + " left the chat!", this);
        }

        Server.removeClient(this);
        try {
            clientSocket.close();
        } catch (IOException ignored) {}
        System.out.println("Client " + (user != null ? user.getUserName() : "Unknown") + " disconnected");
    }

    public User getUser() {
        return user;
    }
}
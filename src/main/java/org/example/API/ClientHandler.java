package org.example.API;

import com.google.gson.Gson;
import org.example.database.DatabaseHelper;
import org.example.projectbackend.Message;
import org.example.model.User;  // تغییر به model

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

        // دریافت JSON از کلاینت و ساخت User
        String userJson = br.readLine();
        this.user = gson.fromJson(userJson, User.class);

        // اضافه کردن کاربر به لیست آنلاین‌ها
        Server.onlineUsers.put(user.getUserId(), user.getUserName());
        System.out.println("Client connected: " + user.getUserName() + " - UUID: " + user.getUserId());

        sendMessage("Welcome to the server, " + user.getUserName() + "!");
        Server.broadcastMessage(user.getUserName() + " joined the chat!", this);
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

    private void handleMessage(String jsonMessage) {
        try {
            Message message = gson.fromJson(jsonMessage, Message.class);

            // ذخیره در دیتابیس - با اسم درست
            DatabaseHelper.savePrivateMessage(message);

            // بقیه logic بدون تغییر...
            String senderName = Server.onlineUsers.getOrDefault(message.getSenderId(), "Unknown");
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            String formattedMessage = "[" + timestamp + "] " + senderName + ": " + message.getContent();

            Server.broadcastMessage(formattedMessage, this);

        } catch (Exception e) {
            System.err.println("Error handling message: " + e.getMessage());
        }
    }

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
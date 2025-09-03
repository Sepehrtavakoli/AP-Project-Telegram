package org.example.API;

import com.google.gson.Gson;
import org.example.projectbackend.Message;
import org.example.projectbackend.User;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

            // پیدا کردن نام کاربر از طریق UUID
            String senderName = Server.onlineUsers.getOrDefault(message.getSenderId(), "Unknown");

            // اضافه کردن timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            String formattedMessage = "[" + timestamp + "] " + senderName + ": " + message.getContent();

            Server.broadcastMessage(formattedMessage, this);

        } catch (Exception e) {
            System.err.println("Error parsing message: " + e.getMessage());
        }
    }

    public void sendMessage(String message) {
        if (isConnected) {
            pw.println(message);
            pw.flush();
        }
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
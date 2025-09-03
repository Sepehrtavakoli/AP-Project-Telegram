package org.example.API;

import com.google.gson.Gson;
import org.example.projectbackend.Message;
import org.example.projectbackend.User;

import java.io.*;
import java.net.Socket;

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

        System.out.println("Client connected: " + user);

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

            // پیدا کردن کاربر فرستنده بر اساس UUID
            String senderName = "Unknown";
            for (ClientHandler client : Server.clientHandlers) {
                if (client.getUser().getUserId().equals(message.getSenderId())) {
                    senderName = client.getUser().getUserName();
                    break;
                }
            }

            String formattedMessage = "[" + message.getTimestamp() + "] " + senderName + ": " + message.getContent();
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
        Server.removeClient(this);
        try {
            clientSocket.close();
        } catch (IOException ignored) {}
        System.out.println("Client " + user.getUserName() + " disconnected");
    }

    public User getUser() {
        return user;
    }
}
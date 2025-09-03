package org.example.API;

import com.google.gson.Gson;
import org.example.projectbackend.Message;
import org.example.projectbackend.User;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private Socket socket;
    private BufferedReader br;
    private PrintWriter pw;
    private boolean isConnected;
    private User user;
    private static final Gson gson = new Gson();
    private MessageListener messageListener;

    public interface MessageListener {
        void onMessageReceived(String message);
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    public boolean connectToServer(String serverHost, int serverPort, User user) {
        this.user = user;

        try {
            socket = new Socket(serverHost, serverPort);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            pw = new PrintWriter(socket.getOutputStream(), true);

            // ارسال User به صورت JSON
            String userJson = gson.toJson(user);
            pw.println(userJson);
            pw.flush();

            startMessageListener();

            isConnected = true;
            System.out.println("Connected to server as: " + user.getUserName());
            return true;
        } catch (IOException e) {
            System.err.println("Failed to connect: " + e.getMessage());
            return false;
        }
    }

    private void startMessageListener() {
        Thread listener = new Thread(() -> {
            try {
                String msg;
                while (isConnected && (msg = br.readLine()) != null) {
                    System.out.println("Received: " + msg);

                    if (messageListener != null) {
                        messageListener.onMessageReceived(msg);
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
        listener.setDaemon(true);
        listener.start();
    }

    public void sendMessage(String message) {
        if (isConnected && message != null && !message.trim().isEmpty()) {
            Message msgObj = new Message(user.getUserId(), message, Message.MessageType.TEXT);
            String jsonMessage = gson.toJson(msgObj);
            pw.println(jsonMessage);
            pw.flush();
        }
    }

    public void closeEverything() {
        isConnected = false;
        try {
            if (br != null) br.close();
            if (pw != null) pw.close();
            if (socket != null) socket.close();
            System.out.println("Client disconnected");
        } catch (IOException ignored) {}
    }

    // برای تست کنسولی
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your username: ");
        String username = scanner.nextLine();

        System.out.print("Enter your phone number: ");
        String phoneNumber = scanner.nextLine();

        User user = new User(username, null, phoneNumber);

        Client client = new Client();
        if (client.connectToServer("localhost", 1234, user)) {
            System.out.println("Connected! Type messages (type '/quit' to exit):");

            client.setMessageListener(new MessageListener() {
                @Override
                public void onMessageReceived(String message) {
                    System.out.println(message);
                }
            });

            String input;
            while ((input = scanner.nextLine()) != null) {
                if (input.equals("/quit")) {
                    client.closeEverything();
                    break;
                }
                client.sendMessage(input);
            }
        }
    }
}
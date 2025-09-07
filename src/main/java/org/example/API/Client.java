package org.example.API;

import com.google.gson.Gson;
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
        void onMessageReceived(String message);
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

    private void startMessageListener() {
        Thread listenerThread = new Thread(() -> {
            try {
                String msg;
                while (isConnected && (msg = br.readLine()) != null) {
                    final String finalMsg = msg; // Final variable for use in lambda
                    // Notify all registered listeners on the JavaFX application thread
                    for (MessageListener listener : listeners) {
                        javafx.application.Platform.runLater(() -> listener.onMessageReceived(finalMsg));
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
        listenerThread.setDaemon(true); // Ensures the thread doesn't prevent the application from exiting
        listenerThread.start();
    }

    // Overloaded method for sending a private message
    public void sendMessage(String message, UUID receiverId) {
        if (isConnected && message != null && !message.trim().isEmpty()) {
            Message msgObj = new Message(user.getUserId(), receiverId, message, Message.MessageType.TEXT);
            String jsonMessage = gson.toJson(msgObj);
            pw.println(jsonMessage);
            pw.flush();
            System.out.println("Sent to: " + receiverId + " | Message: " + jsonMessage);
        }
    }

    // This method can be kept for general purpose or testing
    public void sendMessage(String jsonMessage) {
        if (isConnected && jsonMessage != null && !jsonMessage.trim().isEmpty()) {
            pw.println(jsonMessage);
            pw.flush();
            System.out.println("Generic message sent: " + jsonMessage);
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
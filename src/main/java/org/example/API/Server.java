package org.example.API;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private volatile boolean isRunning;

    public static final List<ClientHandler> clientHandlers = new CopyOnWriteArrayList<>();
    public static final Map<UUID, String> onlineUsers = new ConcurrentHashMap<>();

    public Server(int port) {
        try {
            serverSocket = new ServerSocket(port);
            threadPool = Executors.newCachedThreadPool();
            isRunning = true;
            System.out.println("Server started on port " + port);
        } catch (IOException e) {
            throw new RuntimeException("Could not start server: " + e.getMessage(), e);
        }
    }

    public static ClientHandler findClientByUserId(UUID userId) {
        for (ClientHandler client : clientHandlers) {
            if (client.getUser() != null && client.getUser().getUserId().equals(userId)) {
                return client;
            }
        }
        return null;
    }

    public static void printOnlineUsers() {
        System.out.println("=== Online Users ===");
        for (Map.Entry<UUID, String> entry : onlineUsers.entrySet()) {
            System.out.println(entry.getValue() + " - " + entry.getKey());
        }
        System.out.println("====================");
    }

    public static void sendMessageToUser(UUID userId, String message) {
        ClientHandler targetClient = findClientByUserId(userId);
        if (targetClient != null) {
            targetClient.sendMessage(message); // ارسال پیام متنی ساده
        }
    }

    public void start() {
        Thread acceptThread = new Thread(() -> {
            while (isRunning && !serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(socket);
                    clientHandlers.add(handler);
                    threadPool.execute(handler);
                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("Error accepting client: " + e.getMessage());
                    }
                }
            }
        });
        acceptThread.start();
    }

    public void stop() {
        isRunning = false;
        try {
            for (ClientHandler client : clientHandlers) {
                client.closeEverything();
            }
            clientHandlers.clear();
            onlineUsers.clear();
            if (serverSocket != null) serverSocket.close();
            if (threadPool != null) threadPool.shutdown();
            System.out.println("Server stopped.");
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }

// متد broadcastMessage را برای هماهنگی با ساختار جدید تغییر دهید

    public static void broadcastMessage(String jsonMessage, ClientHandler sender) {
        for (ClientHandler client : clientHandlers) {
            if (client != sender) {
                client.sendMessage(jsonMessage);
            }
        }
        System.out.println("Broadcast: " + jsonMessage);
    }

    public static void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
        System.out.println("Client removed. Total: " + clientHandlers.size());
    }

    public static Map<UUID, String> getOnlineUsers() {
        return onlineUsers;
    }

    public static String getOnlineUserNames() {
        return String.join(", ", onlineUsers.values());
    }

    // متد main را به طور کامل جایگزین کنید

    public static void main(String[] args) {
        // <<-- این خط کد حیاتی، مشکل را حل می‌کند
        // اتصال به دیتابیس برای پروسه سرور برقرار می‌شود
        org.example.database.DatabaseHelper.initializeDatabase();

        Server server = new Server(1234);
        server.start();
    }
}
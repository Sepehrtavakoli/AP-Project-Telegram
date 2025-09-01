package org.example.API;

import javafx.fxml.Initializable;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Initializable {

    private Server server;
    private boolean isRunning = false;
    private ExecutorService threadPool;

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        startServer();
    }

    private ServerSocket serverSocket;
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    public Server() {

    }

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.threadPool = Executors.newCachedThreadPool();
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(1234);
            threadPool = Executors.newCachedThreadPool();
            isRunning = true;

            System.out.println("Server started on port 1234");
            System.out.println("Waiting for clients to connect...");

            Thread acceptThread = new Thread(() -> {
                while (isRunning && !serverSocket.isClosed()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("New Client connected: " + clientSocket.getInetAddress());

                        ClientHandler clientHandler = new ClientHandler(clientSocket);

                        synchronized (clientHandlers) {
                            clientHandlers.add(clientHandler);
                        }

                        threadPool.execute(clientHandler);

                        System.out.println("Total connected clients: " + clientHandlers.size());

                    } catch (IOException e) {
                        if (isRunning) {
                            System.err.println("Error accepting client: " + e.getMessage());
                        }
                        break;
                    }
                }
            });
            acceptThread.setDaemon(true);
            acceptThread.start();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error starting server: " + e.getMessage());
        }
    }

    public void stopServer() {
        isRunning = false;
        try {
            synchronized (clientHandlers) {
                for (ClientHandler client : clientHandlers) {
                    client.closeEverything();
                }
                clientHandlers.clear();
            }

            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            if (threadPool != null){
                threadPool.shutdown();
            }

            System.out.println("Server stopped");
        }catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error stopping server: " + e.getMessage());
        }
    }

    public static void sendMessageToClient(String targetClientName, String message, ClientHandler sender) {
        synchronized (clientHandlers) {
            for (ClientHandler client : clientHandlers) {
                if (client.getClientName() != null &&
                        client.getClientName().equals(targetClientName) &&
                        client != sender) {
                    client.sendMessage(message);
                    break;
                }
            }
        }
    }

    public static void removeClient(ClientHandler clientHandler) {
        synchronized (clientHandlers) {
            clientHandlers.remove(clientHandler);
            System.out.println("Client removed. Total clients: " + clientHandlers.size());
        }
    }

    public void sendMessage(String message) throws IOException {
        try {
            out.write(message);
            out.newLine();
            out.flush();
        }catch (IOException e){
            e.printStackTrace();
            System.out.println("Error Sending Message");
            closeEverything(socket, in, out);
        }
    }

    public void receiveMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (socket.isConnected()) {
                    try {
                        String messageRecived = in.readLine();
                    }catch (IOException e){
                        e.printStackTrace();
                        System.out.println("Error Receiving Message");
                        closeEverything(socket, in, out);
                        break;
                    }
                }
            }
        }).start();
    }

    private void closeEverything(Socket socket, BufferedReader in, BufferedWriter out) {
        try {
            if (socket != null) {
                socket.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        try {
            Server server = new Server(new ServerSocket(1234));
            server.isRunning = true;
            server.threadPool = Executors.newCachedThreadPool();

            System.out.println("Server started on port 1234");

            // Accept clients
            while (server.isRunning) {
                Socket clientSocket = server.serverSocket.accept();
                System.out.println("New client connected");

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandlers.add(clientHandler);
                server.threadPool.execute(clientHandler);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
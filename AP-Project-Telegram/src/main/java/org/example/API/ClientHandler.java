package org.example.API;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private BufferedReader br;
    private BufferedWriter bw;
    private PrintWriter pw;
    private boolean isConnected;
    private String clientName;




    public ClientHandler(Socket socket) {
        try {
            this.clientSocket = socket;
            this.bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.pw = new PrintWriter(socket.getOutputStream(), true);
            this.isConnected = true;

            this.clientName = br.readLine();
            System.out.println("Client name received: " + clientName);

            sendMessage("Welcome to the server, " + clientName + "!");

//            Server.broadcastMessage(clientName + " joined the chat!", this);

        } catch (IOException e) {
            System.err.println("Error setting up client handler: " + e.getMessage());
            closeEverything();
        }
    }


    @Override
    public void run() {
        String msgFromClient = null;

        while (clientSocket.isConnected()&& isConnected) {
            try {
                msgFromClient = br.readLine();

                if (msgFromClient == null) {
                    break;
                }

                System.out.println("Message received from " + clientName + ": " + msgFromClient);

                handleMessage(msgFromClient);

            } catch (Exception e){
                break;
            }
        }
        closeEverything();
    }
    private void handleMessage(String message) {
        if (message.startsWith("/")) {
            handleCommand(message);
        } else {
            String formattedMessage = clientName + ": " + message;
//            Server.broadcastMessage(formattedMessage, this);
        }
    }

    private void handleCommand(String command) {
        String[] parts = command.split(" ", 2);
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "/list":
                sendConnectedUsersList();
                break;

            case "/pm":
                if (parts.length > 1) {
                    handlePrivateMessage(parts[1]);
                } else {
                    sendMessage("Usage: /pm <username> <message>");
                }
                break;

            case "/quit":
                sendMessage("Goodbye!");
                closeEverything();
                break;

            default:
                sendMessage("Unknown command: " + cmd);
                break;
        }
    }

    private void handlePrivateMessage(String pmData) {
        String[] parts = pmData.split(" ", 2);
        if (parts.length < 2) {
            sendMessage("Usage: /pm <username> <message>");
            return;
        }

        String targetUsername = parts[0];
        String message = parts[1];
        String formattedMessage = "[Private from " + clientName + "]: " + message;

        boolean sent = false;
        synchronized (Server.clientHandlers) {
            for (ClientHandler client : Server.clientHandlers) {
                if (client.getClientName() != null &&
                        client.getClientName().equals(targetUsername) &&
                        client != this) {
                    client.sendMessage(formattedMessage);
                    sent = true;
                    break;
                }
            }
        }

        if (sent) {
            sendMessage("Private message sent to " + targetUsername);
        } else {
            sendMessage("User " + targetUsername + " not found or not online");
        }
    }

    private void sendConnectedUsersList() {
        StringBuilder userList = new StringBuilder("Connected users: ");

        synchronized (Server.clientHandlers) {
            for (ClientHandler client : Server.clientHandlers) {
                if (client.getClientName() != null && client != this) {
                    userList.append(client.getClientName()).append(", ");
                }
            }
        }

        String list = userList.toString();
        if (list.endsWith(", ")) {
            list = list.substring(0, list.length() - 2);
        }

        sendMessage(list);
    }

    public void sendMessage(String message) {
        if (pw != null && isConnected) {
            try {
                pw.println(message);
                pw.flush();
            } catch (Exception e) {
                System.err.println("Error sending message to " + clientName + ": " + e.getMessage());
                closeEverything();
            }
        }
    }

    public void closeEverything() {
        isConnected = false;

        Server.removeClient(this);

//        if (clientName != null) {
//            Server.broadcastMessage(clientName + " left the chat.", this);
//        }

        try {
            if (br != null) {
                br.close();
            }
            if (bw != null) {
                bw.close();
            }
            if (pw != null) {
                pw.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing resources for client " + clientName + ": " + e.getMessage());
        }

        System.out.println("Client " + clientName + " disconnected and resources cleaned up");
    }

    public String getClientName() {
        return clientName;
    }

    public boolean isConnected() {
        return isConnected && clientSocket != null && clientSocket.isConnected() && !clientSocket.isClosed();
    }

    public Socket getClientSocket() {
        return clientSocket;
    }
}

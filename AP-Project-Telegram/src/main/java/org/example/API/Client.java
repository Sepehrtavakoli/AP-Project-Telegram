package org.example.API;

import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;

public class Client implements Initializable {

    private Client client;

    private Socket socket;
    private BufferedReader br;
    private BufferedWriter bw;
    private PrintWriter pw;
    private String username;
    private boolean isConnected;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Client initialized, ready to connect");
    }

    public Client() {

    }

    public Client(Socket socket, String username) {
        this.socket = socket;
        this.username = username;
        setupStreams();
    }

    public boolean connectToServer(String serverHost, int serverPort, String username) {
        this.username = username;

        try {
            socket = new Socket(serverHost, serverPort);
            setupStreams();

            sendMessage(username);

            startMessageListener();

            isConnected = true;
            System.out.println("Connected to server as: " + username);
            return true;

        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            return false;
        }
    }

    private void setupStreams() {
        try {
            // Setup input/output streams
            this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.pw = new PrintWriter(socket.getOutputStream(), true);

        } catch (IOException e) {
            System.err.println("Error setting up streams: " + e.getMessage());
            closeEverything();
        }
    }

    private void startMessageListener() {
        Thread messageListener = new Thread(() -> {
            String messageReceived;

            while (isConnected && socket != null && socket.isConnected()) {
                try {
                    messageReceived = br.readLine();

                    if (messageReceived == null) {
                        System.out.println("Server disconnected");
                        break;
                    }

                    handleReceivedMessage(messageReceived);

                } catch (IOException e) {
                    if (isConnected) {
                        System.err.println("Error receiving message: " + e.getMessage());
                    }
                    break;
                }
            }

            closeEverything();
        });

        messageListener.setDaemon(true);
        messageListener.start();
    }

    private void handleReceivedMessage(String message) {
        System.out.println("Received: " + message);
//
//        // If using JavaFX UI, update it on JavaFX Application Thread
//        if (messageDisplayArea != null) {
//            Platform.runLater(() -> {
//                messageDisplayArea.appendText(message + "\n");
//            });
//        }
//
//        // If using VBox for messages
//        if (messageContainer != null) {
//            Platform.runLater(() -> {
//                addMessageToUI(message);
//            });
//        }
   }

//    private void addMessageToUI(String message) {
//        // Add message to VBox - you can customize this based on your UI design
//        javafx.scene.control.Label messageLabel = new javafx.scene.control.Label(message);
//        messageLabel.setWrapText(true);
//        messageLabel.setStyle("-fx-padding: 5; -fx-background-color: #f0f0f0; -fx-background-radius: 5;");
//
//        if (messageContainer != null) {
//            messageContainer.getChildren().add(messageLabel);
//        }
//    }

    public void sendMessage(String messageSend) {
        if (pw != null && isConnected) {
            try {
                pw.println(messageSend);
                pw.flush();
                System.out.println("Message sent: " + messageSend);
            } catch (Exception e) {
                System.err.println("Error sending message: " + e.getMessage());
                closeEverything();
            }
        } else {
            System.out.println("Not connected to server");
        }
    }

//    // Method for JavaFX UI to set message display area
//    public void setMessageDisplayArea(TextArea textArea) {
//        this.messageDisplayArea = textArea;
//    }
//
//    // Method for JavaFX UI to set message container
//    public void setMessageContainer(VBox vBox) {
//        this.messageContainer = vBox;
//    }

    public void closeEverything() {
        isConnected = false;

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
            if (socket != null) {
                socket.close();
            }

            System.out.println("Client disconnected and resources cleaned up");

        } catch (IOException e) {
            System.err.println("Error closing client resources: " + e.getMessage());
        }
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public boolean isConnected() {
        return isConnected && socket != null && socket.isConnected() && !socket.isClosed();
    }

    // Method for testing without JavaFX
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your username: ");
        String username = scanner.nextLine();

        Client client = new Client();

        if (client.connectToServer("localhost", 1234, username)) {
            System.out.println("Connected! Type messages (type 'quit' to exit):");

            String input;
            while (client.isConnected() && !(input = scanner.nextLine()).equals("quit")) {
                client.sendMessage(input);
            }

            client.closeEverything();
        } else {
            System.out.println("Failed to connect to server");
        }

        scanner.close();
    }
}
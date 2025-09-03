package org.example.approjectgui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import org.example.API.Client;
import org.example.projectbackend.User;

import java.net.URL;
import java.util.ResourceBundle;

public class ChatController implements Initializable {

    @FXML
    private VBox messagesContainer;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TextField messageInput;

    private Client client;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // اتصال به سرور
        connectToServer();

        // تنظیم اسکرول به پایین هنگام اضافه شدن پیام جدید
        messagesContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            scrollPane.setVvalue(1.0);
        });

        // ارسال پیام با Enter
        messageInput.setOnAction(event -> sendMessage());
    }

    private void connectToServer() {
        User currentUser = UserData.currentUser;
        if (currentUser != null) {
            client = new Client();
            client.setMessageListener(new Client.MessageListener() {
                @Override
                public void onMessageReceived(String message) {
                    Platform.runLater(() -> {
                        addMessage(message, false);
                    });
                }
            });

            if (client.connectToServer("localhost", 1234, currentUser)) {
                addMessage("Connected to server as: " + currentUser.getUserName(), false);
            } else {
                addMessage("Failed to connect to server", false);
            }
        }
    }

    @FXML
    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty()) {
            client.sendMessage(message);
            addMessage("You: " + message, true);
            messageInput.clear();
        }
    }

    private void addMessage(String message, boolean isOwnMessage) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(isOwnMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 10, 5, 10));

        Label messageLabel = new Label(message);
        messageLabel.setFont(Font.font("Arial", 14));
        messageLabel.setTextFill(Color.BLACK);
        messageLabel.setStyle("-fx-background-color: " + (isOwnMessage ? "#dcf8c6" : "#ffffff") +
                "; -fx-background-radius: 10; -fx-padding: 10;");
        messageLabel.setMaxWidth(300);
        messageLabel.setWrapText(true);

        messageBox.getChildren().add(messageLabel);
        messagesContainer.getChildren().add(messageBox);
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
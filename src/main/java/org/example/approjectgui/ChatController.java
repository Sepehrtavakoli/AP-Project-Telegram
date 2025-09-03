package org.example.approjectgui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.API.Client;
import org.example.projectbackend.User;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ChatController implements Initializable {

    @FXML private VBox messagesContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField messageInput;
    @FXML private Label chatPartnerName;
    @FXML private Label onlineStatus;
    @FXML private Circle onlineIndicator;

    private Client client;
    private Stage stage;
    private String partnerName = "Ali";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupChatUI();
        connectToServer();

        // تنظیم اسکرول اتوماتیک
        messagesContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            scrollPane.setVvalue(1.0);
        });

        // ارسال با Enter
        messageInput.setOnAction(event -> sendMessage());

        // فوکوس روی فیلد پیام
        Platform.runLater(() -> messageInput.requestFocus());
    }

    private void setupChatUI() {
        // نمایش نام کاربر مقابل در هدر
        chatPartnerName.setText(partnerName);
        onlineStatus.setText("online");
        onlineIndicator.setVisible(true);
    }

    private void connectToServer() {
        User currentUser = UserData.currentUser;
        if (currentUser != null) {
            client = new Client();
            client.setMessageListener(new Client.MessageListener() {
                @Override
                public void onMessageReceived(String message) {
                    Platform.runLater(() -> {
                        try {
                            // فیلتر پیام‌های سیستم (Welcome, joined, left)
                            if (message.contains("joined the chat") ||
                                    message.contains("left the chat") ||
                                    message.contains("Welcome to")) {
                                // نمایش پیام سیستم با استایل متفاوت
                                addSystemMessage(message);
                                return;
                            }

                            // پردازش پیام‌های معمولی
                            if (message.contains("]") && message.contains(":")) {
                                int timeEndIndex = message.indexOf("]");
                                int nameEndIndex = message.indexOf(":", timeEndIndex + 1);

                                if (timeEndIndex != -1 && nameEndIndex != -1) {
                                    String timePart = message.substring(0, timeEndIndex + 1);
                                    String senderName = message.substring(timeEndIndex + 2, nameEndIndex).trim();
                                    String messageContent = message.substring(nameEndIndex + 1).trim();

                                    // تنظیم نام partner اگر پیام از اوست
                                    if (!senderName.equals(currentUser.getUserName())) {
                                        setPartnerName(senderName);
                                    }

                                    // نمایش پیام
                                    String displayMessage = timePart + " " + messageContent;
                                    addMessage(displayMessage, senderName.equals(currentUser.getUserName()));
                                } else {
                                    // اگر فرمت پیام غیرمنتظره بود
                                    addMessage(message, false);
                                }
                            } else {
                                // پیام‌های بدون فرمت مشخص
                                addMessage(message, false);
                            }
                        } catch (Exception e) {
                            System.err.println("Error processing message: " + e.getMessage());
                            addMessage("Error displaying message", false);
                        }
                    });
                }
            });

            // اتصال به سرور
            boolean connected = client.connectToServer("localhost", 1234, currentUser);
            if (connected) {
                System.out.println("Connected to server successfully as: " + currentUser.getUserName());
                addSystemMessage("Connected to server");
            } else {
                System.err.println("Failed to connect to server");
                addSystemMessage("Failed to connect to server");
            }
        } else {
            System.err.println("Current user is null - cannot connect to server");
            addSystemMessage("User not logged in - please restart the application");
        }
    }

    @FXML
    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty()) {
            client.sendMessage(message);

            // ایجاد پیام با timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            String messageWithTime = "[" + timestamp + "] " + message;

            addMessage(messageWithTime, true);
            messageInput.clear();
        }
    }

    private void addMessage(String message, boolean isOwnMessage) {
        try {
            HBox messageBox = new HBox();
            messageBox.setAlignment(isOwnMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            messageBox.setPadding(new Insets(8, 15, 8, 15));
            messageBox.setMaxWidth(380);

            VBox messageContent = new VBox();
            messageContent.setAlignment(isOwnMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            messageContent.setSpacing(3);

            Label messageLabel = new Label(message);
            messageLabel.setFont(Font.font("Arial", 14));
            messageLabel.setTextFill(isOwnMessage ? Color.WHITE : Color.WHITE); // متن سفید برای خوانایی بهتر
            messageLabel.setStyle("-fx-background-color: " + (isOwnMessage ? "#0088cc" : "#2ea6ff") +
                    "; -fx-background-radius: 12; -fx-padding: 10 15 10 15;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);");
            messageLabel.setMaxWidth(280);
            messageLabel.setWrapText(true);

            messageContent.getChildren().add(messageLabel);
            messageBox.getChildren().add(messageContent);
            messagesContainer.getChildren().add(messageBox);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            // قطع connection به سرور
            if (client != null) {
                client.closeEverything();
            }

            Parent root = FXMLLoader.load(getClass().getResource("HomePage.fxml"));
            Stage stage = (Stage) messageInput.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addSystemMessage(String message) {
        Label systemLabel = new Label(message);
        systemLabel.setFont(Font.font("Arial Italic", 12));
        systemLabel.setTextFill(Color.GRAY);
        systemLabel.setAlignment(Pos.CENTER);
        systemLabel.setMaxWidth(380);
        systemLabel.setPadding(new Insets(5, 0, 5, 0));

        HBox systemBox = new HBox(systemLabel);
        systemBox.setAlignment(Pos.CENTER);
        systemBox.setPadding(new Insets(5, 0, 5, 0));

        messagesContainer.getChildren().add(systemBox);
    }

    @FXML
    private void handleCall() {
        System.out.println("Call button clicked");
    }

    @FXML
    private void handleSearch() {
        System.out.println("Search button clicked");
    }

    @FXML
    private void handleMenu() {
        System.out.println("Menu button clicked");
    }

    @FXML
    private void handleAttachFile() {
        System.out.println("Attach file button clicked");
    }

    @FXML
    private void handleEmoji() {
        System.out.println("Emoji button clicked");
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
        chatPartnerName.setText(partnerName);
    }
}
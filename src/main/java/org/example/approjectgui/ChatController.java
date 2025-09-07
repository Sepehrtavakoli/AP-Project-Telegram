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
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.example.API.Client;
import org.example.API.ClientManager;
import org.example.database.DatabaseHelper;
import org.example.model.User;
import org.example.projectbackend.Message;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class ChatController implements Initializable, Client.MessageListener {

    // FXML fields restored
    @FXML private VBox messagesContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField messageInput;
    @FXML private Label chatPartnerName;
    @FXML private Label onlineStatus;
    @FXML private Circle onlineIndicator;
    @FXML private ImageView BackButton;

    private Client client;
    private UUID currentPartnerId;
    private String currentPartnerName;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.client = ClientManager.getInstance();
        if (this.client != null) {
            this.client.addMessageListener(this);
        }

        messagesContainer.heightProperty().addListener((obs, oldVal, newVal) -> Platform.runLater(() -> scrollPane.setVvalue(1.0)));
        messageInput.setOnAction(event -> sendMessage());
        scrollPane.setFitToWidth(true);
    }

    @Override
    public void onMessageReceived(Message message) {
        if (message.getType() == Message.MessageType.SYSTEM) {
            addSystemMessage(message.getContent());
            return;
        }

        // چک کردن فرستنده با UUID
        if (currentPartnerId != null && message.getSenderId() != null && message.getSenderId().equals(currentPartnerId)) {
            String timestamp = message.getTimestamp();
            String content = message.getContent();
            // <<-- نام مخاطب را به پیام دریافتی اضافه می‌کنیم
            String displayMessage = "[" + timestamp + "] " + this.currentPartnerName + ": " + content;
            addMessage(displayMessage, false); // false = پیام دریافتی
        }
    }

    @FXML
    private void sendMessage() {
        String messageText = messageInput.getText().trim();
        if (!messageText.isEmpty()) {
            if (client != null && client.isConnected()) {
                client.sendMessage(messageText, currentPartnerId);

                // Display our own sent message
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                String displayMessage = "[" + timestamp + "] You: " + messageText;
                addMessage(displayMessage, true); // true = our own message
            } else {
                addSystemMessage("Error: Not connected to server.");
            }
            messageInput.clear();
        }
    }

    @FXML
    private void handleBack() {
        if (this.client != null) {
            this.client.removeMessageListener(this);
        }
        try {
            Parent root = FXMLLoader.load(getClass().getResource("HomePage.fxml"));
            Stage stage = (Stage) messageInput.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPartner(String partnerName, UUID partnerId) {
        this.currentPartnerName = partnerName;
        this.currentPartnerId = partnerId;

        // Set UI elements
        this.chatPartnerName.setText(partnerName);
        this.onlineStatus.setText("online");
        this.onlineIndicator.setVisible(true);

        Platform.runLater(this::loadChatHistory);
    }

    private void loadChatHistory() {
        messagesContainer.getChildren().clear();
        if (UserData.currentUser != null && currentPartnerId != null) {
            List<Message> history = DatabaseHelper.getPrivateMessages(UserData.currentUser.getUserId(), currentPartnerId);
            for (Message msg : history) {
                boolean isOwn = msg.getSenderId().equals(UserData.currentUser.getUserId());
                // <<-- نام فرستنده را به درستی برای پیام‌های تاریخچه هم تنظیم می‌کنیم
                String senderPrefix = isOwn ? "You: " : this.currentPartnerName + ": ";
                String displayMessage = "[" + msg.getTimestamp() + "] " + senderPrefix + msg.getContent();
                addMessage(displayMessage, isOwn);
            }
        }
    }

    private void addMessage(String message, boolean isOwnMessage) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(isOwnMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 10, 5, 10));

        Text textNode = new Text(message);
        textNode.setFont(Font.font("Arial", 14));
        TextFlow bubble = new TextFlow(textNode);
        bubble.setPadding(new Insets(8, 12, 8, 12));

        String bubbleStyle = isOwnMessage ?
                "-fx-background-color: #dcf8c6; -fx-background-radius: 10 10 0 10;" :
                "-fx-background-color: #ffffff; -fx-background-radius: 10 10 10 0;";
        bubble.setStyle(bubbleStyle + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);");

        bubble.setMaxWidth(300);
        messageBox.getChildren().add(bubble);
        messagesContainer.getChildren().add(messageBox);
    }

    private void addSystemMessage(String message) {
        Label systemLabel = new Label(message);
        systemLabel.setFont(Font.font("Arial Italic", 12));
        systemLabel.setTextFill(Color.GRAY);
        HBox systemBox = new HBox(systemLabel);
        systemBox.setAlignment(Pos.CENTER);
        systemBox.setPadding(new Insets(5, 0, 5, 0));
        messagesContainer.getChildren().add(systemBox);
    }

    // Stub methods for FXML linking restored
    @FXML private void handleCall() { System.out.println("Call button clicked"); }
    @FXML private void handleSearch() { System.out.println("Search button clicked"); }
    @FXML private void handleMenu() { System.out.println("Menu button clicked"); }
    @FXML private void handleAttachFile() { System.out.println("Attach file button clicked"); }
    @FXML private void handleEmoji() { System.out.println("Emoji button clicked"); }
}
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// <<-- مهم: پیاده‌سازی اینترفیس MessageListener
public class ChatController implements Initializable, Client.MessageListener {

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
    private Stage stage;
    private String partnerName = "User";

    private static final Pattern MSG_PATTERN =
            Pattern.compile("^\\[(\\d{2}:\\d{2})]\\s([^:]+):\\s([\\s\\S]*)$", Pattern.DOTALL);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupChatUI();

        // دریافت کلاینت سراسری و ثبت شدن به عنوان شنونده
        this.client = ClientManager.getInstance();
        if (this.client != null) {
            this.client.addMessageListener(this);
        }

        messagesContainer.heightProperty().addListener((obs, oldVal, newVal) ->
                Platform.runLater(() -> scrollPane.setVvalue(1.0)));

        messageInput.setOnAction(event -> sendMessage());
        Platform.runLater(() -> messageInput.requestFocus());
        scrollPane.setFitToWidth(true);
        messagesContainer.setFillWidth(true);
    }

    // <<-- متد جدید برای دریافت پیام‌ها از کلاینت سراسری
    @Override
    public void onMessageReceived(String message) {
        User currentUser = UserData.currentUser;
        if (currentUser == null) return;

        try {
            if (message.contains("joined the chat") || message.contains("left the chat") || message.contains("Welcome to")) {
                addSystemMessage(message);
                return;
            }

            Matcher m = MSG_PATTERN.matcher(message);
            if (m.matches()) {
                String senderName = m.group(2).trim();

                // شرط کلیدی: فقط پیام‌های مربوط به این چت را پردازش کن
                if (senderName.equalsIgnoreCase(currentPartnerName)) {
                    processIncomingMessage(message, currentUser);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in ChatController.onMessageReceived: " + e.getMessage());
        }
    }

    private void processIncomingMessage(String message, User currentUser) {
        try {
            Matcher m = MSG_PATTERN.matcher(message);
            if (m.matches()) {
                String timePart = m.group(1);
                String senderName = m.group(2).trim();
                String messageContent = m.group(3);

                boolean isOwnMessage = senderName.equals(currentUser.getUserName()) || senderName.equals("You");

                if (!isOwnMessage) {
                    setPartnerName(senderName);
                }

                String displayMessage = "[" + timePart + "] " + messageContent;
                addMessage(displayMessage, isOwnMessage);
            } else {
                addMessage(message, false);
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            addMessage("Error displaying message", false);
        }
    }

    @FXML
    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty()) {
            System.out.println("Sending message: " + message + " to user " + currentPartnerId);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            String messageWithTime = "[" + timestamp + "] You: " + message;

            if (client != null && client.isConnected()) {
                client.sendMessage(message, currentPartnerId);
            } else {
                addSystemMessage("Error: Not connected to server.");
            }

            addMessage(messageWithTime, true);
            messageInput.clear();
        }
    }

    @FXML
    private void handleBack() {
        // <<-- مهم: هنگام بازگشت، فقط خود را از لیست شنونده‌ها حذف کن
        if (this.client != null) {
            this.client.removeMessageListener(this);
        }

        try {
            Platform.runLater(() -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/approjectgui/HomePage.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) messageInput.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addMessage(String message, boolean isOwnMessage) {
        try {
            HBox messageBox = new HBox();
            messageBox.setAlignment(isOwnMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            messageBox.setPadding(new Insets(8, 15, 8, 15));
            messageBox.setMaxWidth(Double.MAX_VALUE);

            VBox messageContent = new VBox();
            messageContent.setAlignment(isOwnMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            messageContent.setSpacing(3);

            Text textNode = new Text(message);
            textNode.setFill(isOwnMessage ? Color.WHITE : Color.BLACK);
            textNode.setFont(Font.font("Arial", 14));

            TextFlow bubble = new TextFlow(textNode);
            bubble.setPadding(new Insets(10, 15, 10, 15));
            bubble.setStyle("-fx-background-color: " + (isOwnMessage ? "#0088cc" : "#ffffff") +
                    "; -fx-background-radius: 12;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);");

            bubble.maxWidthProperty().bind(scrollPane.widthProperty().multiply(0.75));
            textNode.wrappingWidthProperty().bind(bubble.maxWidthProperty().subtract(30));

            messageContent.getChildren().add(bubble);
            messageBox.getChildren().add(messageContent);
            messagesContainer.getChildren().add(messageBox);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPartner(String partnerName, UUID partnerId) {
        this.currentPartnerName = partnerName;
        this.currentPartnerId = partnerId;
        setPartnerName(partnerName);
        Platform.runLater(this::loadChatHistory);
    }

    private void loadChatHistory() {
        if (UserData.currentUser != null && currentPartnerId != null) {
            List<Message> history = DatabaseHelper.getPrivateMessages(
                    UserData.currentUser.getUserId(),
                    currentPartnerId
            );

            for (Message msg : history) {
                boolean isOwn = msg.getSenderId().equals(UserData.currentUser.getUserId());
                // Note: For history, we can't get the live sender name easily, so we use partner name
                String senderName = isOwn ? "You" : this.currentPartnerName;
                String timestamp = msg.getTimestamp() != null ? msg.getTimestamp() : "00:00";
                // Re-create the display format for consistency
                String displayText = "[" + timestamp + "] " + senderName + ": " + msg.getContent();
                addMessage(displayText, isOwn);
            }
        }
    }

    private void setupChatUI() {
        chatPartnerName.setText(partnerName);
        onlineStatus.setText("online");
        onlineIndicator.setVisible(true);
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

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setPartnerName(String name) {
        this.partnerName = name;
        if(chatPartnerName != null) {
            chatPartnerName.setText(name);
        }
    }

    // Unused methods
    @FXML private void handleCall() { System.out.println("Call button clicked"); }
    @FXML private void handleSearch() { System.out.println("Search button clicked"); }
    @FXML private void handleMenu() { System.out.println("Menu button clicked"); }
    @FXML private void handleAttachFile() { System.out.println("Attach file button clicked"); }
    @FXML private void handleEmoji() { System.out.println("Emoji button clicked"); }
}
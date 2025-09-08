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
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.example.API.Client;
import org.example.API.ClientManager;
import org.example.database.DatabaseHelper;
import org.example.model.Chanel;
import org.example.model.User;
import org.example.projectbackend.Message;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class ChanelChatController implements Initializable, Client.MessageListener {

    @FXML private VBox messagesContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField messageInput;
    @FXML private Label chanelNameLabel;
    @FXML private Label memberCountLabel;
    @FXML private Circle onlineIndicator;

    private Client client;
    private Chanel currentChanel;
    private boolean isCreator = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.client = ClientManager.getInstance();
        if (this.client != null) {
            this.client.addMessageListener(this);
        }

        messagesContainer.heightProperty().addListener((obs, oldVal, newVal) ->
                Platform.runLater(() -> scrollPane.setVvalue(1.0)));

        scrollPane.setFitToWidth(true);
        setupBackground();

        // بررسی وضعیت پیام‌رسانی رو به بعد از setChanel موکول می‌کنیم
    }

    public void setChanel(Chanel chanel) {
        this.currentChanel = chanel;

        // تنظیم UI
        this.chanelNameLabel.setText(chanel.getChanelName());
        this.memberCountLabel.setText(getMemberCount() + " subscribers");
        this.onlineIndicator.setVisible(true);

        // بررسی آیا کاربر فعلی سازنده کانال است
        if (UserData.currentUser != null) {
            isCreator = currentChanel.getCreatorID().equals(UserData.currentUser.getUserId());
            updateMessageInputStatus();
        }

        Platform.runLater(this::loadChanelMessages);
    }

    private void updateMessageInputStatus() {
        if (!isCreator) {
            messageInput.setDisable(true);
            messageInput.setPromptText("Only channel owner can post messages");
            messageInput.setStyle("-fx-background-color: #f9f9f9; -fx-text-fill: #999999;");
        } else {
            messageInput.setDisable(false);
            messageInput.setPromptText("Type message here...");
            messageInput.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #000000;");
        }
    }

    private void setupBackground() {
        messagesContainer.setStyle("-fx-background-color: #f0f0f0;");
        scrollPane.setStyle("-fx-background: #f0f0f0; -fx-background-color: #f0f0f0;");
    }

    private int getMemberCount() {
        if (currentChanel != null) {
            return DatabaseHelper.getChanelSubscribers(currentChanel.getChanelID()).size();
        }
        return 0;
    }

    private void loadChanelMessages() {
        messagesContainer.getChildren().clear();

        if (currentChanel != null) {
            List<Message> messages = DatabaseHelper.getChanelMessages(currentChanel.getChanelID());

            for (Message message : messages) {
                boolean isOwn = message.getSenderId().equals(UserData.currentUser.getUserId());
                String senderName = isOwn ? "You" : DatabaseHelper.getUserById(message.getSenderId()).getUserName();

                addMessage("[" + message.getTimestamp() + "] " + senderName + ": " + message.getContent(), isOwn);
            }
        }

    }

    @FXML
    private void sendMessage() {
        if (currentChanel == null) {
            System.out.println("Channel not set yet");
            return;
        }

        if (!isCreator) {
            System.out.println("Only channel owner can send messages");
            return;
        }

        String messageText = messageInput.getText().trim();
        if (!messageText.isEmpty()) {
            if (client != null && client.isConnected()) {
                // ایجاد پیام کانال
                Message channelMessage = new Message(
                        UserData.currentUser.getUserId(),
                        null, // receiverId null برای کانال
                        messageText,
                        Message.MessageType.TEXT
                );

                // ذخیره در دیتابیس اول
                boolean saved = DatabaseHelper.saveChanelMessage(currentChanel.getChanelID(), channelMessage);

                if (saved) {
                    // ارسال پیام به سرور
                    client.sendMessage(channelMessage);

                    // نمایش پیام خودمان
                    addMessage("[" + channelMessage.getTimestamp() + "] You: " + messageText, true);
                } else {
                    addSystemMessage("Error: Failed to save message.");
                }
            } else {
                addSystemMessage("Error: Not connected to server.");
            }
            messageInput.clear();
        }
    }

    private void addMessage(String message, boolean isOwnMessage) {
        Platform.runLater(() -> {
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
        });
    }

    private void addSystemMessage(String message) {
        Platform.runLater(() -> {
            Label systemLabel = new Label(message);
            systemLabel.setFont(Font.font("Arial Italic", 12));
            systemLabel.setTextFill(Color.GRAY);
            HBox systemBox = new HBox(systemLabel);
            systemBox.setAlignment(Pos.CENTER);
            systemBox.setPadding(new Insets(5, 0, 5, 0));
            messagesContainer.getChildren().add(systemBox);
        });
    }

    @FXML
    private void handleBack() {
        if (this.client != null) {
            this.client.removeMessageListener(this);
        }
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/org/example/approjectgui/HomePage.fxml"));
            Stage stage = (Stage) messageInput.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(Message message) {
        // فقط پیام‌های کانال رو پردازش کن (receiverId = null)
        if (message.getReceiverId() != null) {
            return;
        }

        Platform.runLater(() -> {
            // بررسی کن که پیام مربوط به همین کانال هست
            if (currentChanel != null) {
                String senderName = DatabaseHelper.getUserById(message.getSenderId()).getUserName();
                addMessage("[" + message.getTimestamp() + "] " + senderName + ": " + message.getContent(), false);
            }
        });
    }
}
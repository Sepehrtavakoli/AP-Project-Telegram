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
import javafx.scene.image.Image;
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
import org.example.model.Group;
import org.example.model.User;
import org.example.projectbackend.GroupMessage;
import org.example.projectbackend.Message;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class GroupChatController implements Initializable, Client.MessageListener {

    @FXML private VBox messagesContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField messageInput;
    @FXML private Label groupNameLabel;
    @FXML private Label memberCountLabel;
    @FXML private Circle onlineIndicator;

    private Client client;
    private Group currentGroup;
    private UUID currentGroupId;
    private String currentGroupName;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.client = ClientManager.getInstance();
        if (this.client != null) {
            this.client.addMessageListener(this);
        }

        messagesContainer.heightProperty().addListener((obs, oldVal, newVal) ->
                Platform.runLater(() -> scrollPane.setVvalue(1.0)));
        messageInput.setOnAction(event -> sendMessage());
        scrollPane.setFitToWidth(true);
    }

    @Override
    public void onMessageReceived(Message message) {
        // اگر پیام receiverId نداشته باشد یا فرستنده آن مشخص نباشد، آن را نادیده می‌گیریم.
        if (message.getReceiverId() == null || message.getSenderId() == null) {
            return;
        }

        // ۱. بررسی می‌کنیم که آیا ID گیرنده پیام (که ID گروه است) با ID گروه فعلی یکی است.
        // ۲. بررسی می‌کنیم که فرستنده پیام، خود کاربر فعلی نباشد (چون پیام‌های خودمان را قبلاً نمایش داده‌ایم).
        if (message.getReceiverId().equals(this.currentGroupId) && !message.getSenderId().equals(UserData.currentUser.getUserId())) {

            // چون این کد در ترد بک‌گراند کلاینت اجرا می‌شود، آپدیت UI را به ترد اصلی JavaFX منتقل می‌کنیم.
            Platform.runLater(() -> {
                // نام فرستنده را از دیتابیس می‌خوانیم.
                User sender = DatabaseHelper.getUserById(message.getSenderId());
                String senderName = (sender != null) ? sender.getUserName() : "Unknown";

                // پیام را برای نمایش آماده می‌کنیم.
                String displayMessage = "[" + message.getTimestamp() + "] " + senderName + ": " + message.getContent();

                // پیام را در UI نمایش می‌دهیم (false یعنی پیام از طرف دیگران است).
                addMessage(displayMessage, false);
            });
        }
    }

    public void setGroup(Group group) {
        this.currentGroup = group;
        this.currentGroupId = group.getGroupId();
        this.currentGroupName = group.getGroupName();

        // تنظیم UI
        this.groupNameLabel.setText(group.getGroupName());
        this.memberCountLabel.setText(getMemberCount() + " members");
        this.onlineIndicator.setVisible(true);

        Platform.runLater(this::loadGroupChatHistory);
    }


    private int getMemberCount() {
        if (currentGroupId != null) {
            // تعداد اعضا را از دیتابیس بر اساس ID گروه فعلی می‌خواند
            return DatabaseHelper.getGroupMembers(currentGroupId).size();
        }
        // اگر به هر دلیلی ID گروه وجود نداشت، صفر برمی‌گرداند
        return 0;
    }

    // در GroupChatController.java این متدها رو اضافه کنید:
    private void loadGroupChatHistory() {
        messagesContainer.getChildren().clear();
        if (UserData.currentUser != null && currentGroupId != null) {
            List<GroupMessage> history = DatabaseHelper.getGroupMessages(currentGroupId);
            for (GroupMessage msg : history) {
                boolean isOwn = msg.getSenderId().equals(UserData.currentUser.getUserId());

                String senderName = isOwn ? "You" : DatabaseHelper.getUserById(msg.getSenderId()).getUserName();
                String displayMessage = "[" + msg.getTimestamp() + "] " + senderName + ": " + msg.getContent();

                addMessage(displayMessage, isOwn);
            }
        }
    }

    private void sendGroupMessage(GroupMessage groupMessage) {
        if (client != null && client.isConnected()) {
            client.sendGroupMessage(groupMessage);

            // نمایش پیام خودمان بلافاصله
            String displayMessage = "[" + groupMessage.getTimestamp() + "] You: " + groupMessage.getContent();
            addMessage(displayMessage, true);
        }
    }

// In class: GroupChatController.java

    @FXML
    private void sendMessage() {
        String messageText = messageInput.getText().trim();
        if (!messageText.isEmpty()) {
            if (client != null && client.isConnected()) {
                // ۱. آبجکت پیام گروهی را می‌سازیم
                GroupMessage groupMessage = new GroupMessage(
                        currentGroupId,
                        UserData.currentUser.getUserId(),
                        messageText,
                        GroupMessage.MessageType.TEXT
                );

                // ۲. پیام را برای ارسال و نمایش به متد کمکی می‌فرستیم
                sendGroupMessage(groupMessage);

                // <<-- این بخش تکراری حذف شد -->>
                // دیگر نیازی به افزودن دستی پیام در اینجا نیست،
                // چون این کار در متد sendGroupMessage انجام می‌شود.

            } else {
                addSystemMessage("Error: Not connected to server.");
            }
            messageInput.clear();
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

    @FXML
    private void handleAttachFile() {
        // پیاده‌سازی ارسال فایل در گروه
        System.out.println("Attach file in group");
    }

}
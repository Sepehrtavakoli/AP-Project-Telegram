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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.API.Client;
import org.example.database.DatabaseHelper;
import org.example.model.User;
import org.example.projectbackend.Message;
import org.example.projectbackend.Message.MessageType;
import javafx.stage.FileChooser;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;


import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class ChatController implements Initializable {

    @FXML private VBox messagesContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField messageInput;
    @FXML private Label chatPartnerName;

    private Client client;
    private Stage stage;

    // انواع چت
    private enum ChatType { PRIVATE, GROUP, CHANNEL }
    private ChatType chatType = ChatType.PRIVATE;

    // شناسه‌ها
    private UUID partnerUserId;
    private UUID groupId;
    private UUID channelId;
    private boolean channelOwner = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        scrollPane.setFitToWidth(true);
        messagesContainer.setFillWidth(true);

        // همیشه آخرین پیام دیده بشه
        messagesContainer.heightProperty().addListener((obs, oldVal, newVal) ->
                Platform.runLater(() -> scrollPane.setVvalue(scrollPane.getVmax())));

        // ارسال با Enter
        messageInput.setOnAction(event -> sendMessage());

        // فوکوس خودکار روی input
        Platform.runLater(() -> messageInput.requestFocus());
    }

    // 📌 باز کردن چت خصوصی
    public void openAsPrivate(UUID partnerUserId, String partnerName) {
        this.chatType = ChatType.PRIVATE;
        this.partnerUserId = partnerUserId;
        chatPartnerName.setText(partnerName);
        loadHistory();
    }

    // 📌 باز کردن گروه
    public void openAsGroup(UUID groupId, String groupName) {
        this.chatType = ChatType.GROUP;
        this.groupId = groupId;
        chatPartnerName.setText(groupName);
        loadHistory();
    }

    // 📌 باز کردن کانال
    public void openAsChannel(UUID channelId, String channelName, boolean isOwner) {
        this.chatType = ChatType.CHANNEL;
        this.channelId = channelId;
        this.channelOwner = isOwner;
        chatPartnerName.setText(channelName + (isOwner ? " (owner)" : ""));
        loadHistory();

        // اگر owner نباشه، input غیرفعال بشه
        messageInput.setDisable(!isOwner);
    }

    // 📌 تاریخچه از دیتابیس
    private void loadHistory() {
        messagesContainer.getChildren().clear();

        List<Message> history = switch (chatType) {
            case PRIVATE -> DatabaseHelper.getPrivateMessages(UserData.currentUser.getUserId(), partnerUserId);
            case GROUP -> DatabaseHelper.getGroupMessages(groupId);
            case CHANNEL -> DatabaseHelper.getChannelPosts(channelId);
        };

        for (Message m : history) {
            addMessageBubble(m);
        }
        Platform.runLater(() -> scrollPane.setVvalue(scrollPane.getVmax()));

    }

    // 📌 ارسال پیام
    @FXML
    private void sendMessage() {
        String content = messageInput.getText().trim();
        if (content.isEmpty()) return;

        Message msg = new Message(UserData.currentUser.getUserId(), null, content, MessageType.TEXT);

        switch (chatType) {
            case PRIVATE -> {
                msg.setReceiverId(partnerUserId);
                DatabaseHelper.savePrivateMessage(msg);
            }
            case GROUP -> DatabaseHelper.saveGroupMessage(groupId, UserData.currentUser.getUserId(), content, MessageType.TEXT);
            case CHANNEL -> {
                if (channelOwner) {
                    DatabaseHelper.saveChannelPost(channelId, UserData.currentUser.getUserId(), content, MessageType.TEXT);
                } else {
                    addSystemMessage("Only owner can post in channel.");
                    return;
                }
            }
        }

        addMessageBubble(msg);
        messageInput.clear();
    }

    private void addMessageBubble(Message msg) {
        boolean isOwn = msg.getSenderId().equals(UserData.currentUser.getUserId());

        HBox bubble = new HBox();
        bubble.setAlignment(isOwn ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        bubble.setPadding(new Insets(5, 10, 5, 10));

        switch (msg.getType()) {
            case TEXT -> {
                Label label = new Label(msg.getContent());
                label.setWrapText(true);
                label.setStyle("-fx-background-color: " + (isOwn ? "#0088cc" : "#e0f7fa") +
                        "; -fx-text-fill: " + (isOwn ? "white" : "black") +
                        "; -fx-padding: 8 12; -fx-background-radius: 12;");
                bubble.getChildren().add(label);
            }
            case IMAGE -> {
                ImageView imageView = new ImageView(new File(msg.getFilePath()).toURI().toString());
                imageView.setFitWidth(150);
                imageView.setPreserveRatio(true);
                bubble.getChildren().add(imageView);
            }
            case FILE -> {
                Label fileLabel = new Label("📎 " + msg.getContent());
                fileLabel.setStyle("-fx-text-fill: blue; -fx-underline: true;");
                fileLabel.setOnMouseClicked(e -> {
                    try {
                        java.awt.Desktop.getDesktop().open(new File(msg.getFilePath())); // باز کردن فایل
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                bubble.getChildren().add(fileLabel);
            }
        }

        messagesContainer.getChildren().add(bubble);
        scrollPane.setVvalue(1.0);
    }




    // 📌 پیام سیستمی
    private void addSystemMessage(String message) {
        Label systemLabel = new Label(message);
        systemLabel.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
        HBox systemBox = new HBox(systemLabel);
        systemBox.setAlignment(Pos.CENTER);
        messagesContainer.getChildren().add(systemBox);
        Platform.runLater(() -> scrollPane.setVvalue(scrollPane.getVmax()));
    }

    // 📌 برگشت به HomePage
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/approjectgui/HomePage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) messageInput.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 📌 سازگاری با HomePageController قدیمی
    public void setPartnerName(String name) {
        chatPartnerName.setText(name);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleAttachFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File or Image");

        // همه نوع فایل مجاز
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(messageInput.getScene().getWindow());

        if (file != null) {
            String fileName = file.getName().toLowerCase();
            Message msg;

            // 🔹 اگر عکس بود
            if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".gif")) {
                msg = new Message(UserData.currentUser.getUserId(), partnerUserId, "[Image]", Message.MessageType.IMAGE);
            }
            // 🔹 اگر فایل دیگه بود
            else {
                msg = new Message(UserData.currentUser.getUserId(), partnerUserId, file.getName(), Message.MessageType.FILE);
            }

            msg.setFilePath(file.getAbsolutePath()); // مسیر کامل فایل
            saveMessage(msg);   // ذخیره در دیتابیس
            addMessageBubble(msg); // نمایش در UI
        }
    }


    @FXML
    private void handleEmoji() {
        FileChooser imageChooser = new FileChooser();
        imageChooser.setTitle("Select Image");
        imageChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File imageFile = imageChooser.showOpenDialog(messageInput.getScene().getWindow());

        if (imageFile != null) {
            Message msg = new Message(UserData.currentUser.getUserId(), partnerUserId, "[Image]", MessageType.IMAGE);
            msg.setFilePath(imageFile.getAbsolutePath());
            saveMessage(msg);
            addMessageBubble(msg);
        }
    }

    // 📌 یک متد کمکی برای ذخیره پیام در دیتابیس
    private void saveMessage(Message msg) {
        switch (chatType) {
            case PRIVATE -> {
                msg.setReceiverId(partnerUserId);
                DatabaseHelper.savePrivateMessage(msg);
            }
            case GROUP -> DatabaseHelper.saveGroupMessage(groupId, msg.getSenderId(), msg.getContent(), msg.getType());
            case CHANNEL -> {
                if (channelOwner) {
                    DatabaseHelper.saveChannelPost(channelId, msg.getSenderId(), msg.getContent(), msg.getType());
                } else {
                    addSystemMessage("Only owner can post in channel.");
                }
            }
        }
    }

}

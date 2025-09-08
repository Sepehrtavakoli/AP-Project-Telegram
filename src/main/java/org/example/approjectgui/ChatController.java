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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.API.Client;
import org.example.API.ClientManager;
import org.example.database.DatabaseHelper;
import org.example.model.Group;
import org.example.model.User;
import org.example.projectbackend.GroupMessage;
import org.example.projectbackend.Message;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class ChatController implements Initializable, Client.MessageListener {

    @FXML private VBox messagesContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField messageInput;
    @FXML private Label chatPartnerName;
    @FXML private Label onlineStatus;

    private Client client;
    private enum ChatMode { PRIVATE, GROUP }
    private ChatMode currentMode;
    private UUID currentTargetId;

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

    public void initPrivateChat(User partner) {
        this.currentMode = ChatMode.PRIVATE;
        this.currentTargetId = partner.getUserId();
        this.chatPartnerName.setText(partner.getUserName());
        this.onlineStatus.setText("online");
        this.onlineStatus.setVisible(true);
        Platform.runLater(this::loadChatHistory);
    }

    public void initGroupChat(Group group) {
        this.currentMode = ChatMode.GROUP;
        this.currentTargetId = group.getGroupId();
        this.chatPartnerName.setText(group.getGroupName());
        int memberCount = DatabaseHelper.getGroupMembers(this.currentTargetId).size();
        this.onlineStatus.setText(memberCount + " members");
        this.onlineStatus.setVisible(true);
        Platform.runLater(this::loadChatHistory);
    }

    private void loadChatHistory() {
        messagesContainer.getChildren().clear();
        if (UserData.currentUser == null || currentTargetId == null) return;

        if (currentMode == ChatMode.PRIVATE) {
            List<Message> history = DatabaseHelper.getPrivateMessages(UserData.currentUser.getUserId(), currentTargetId);
            for (Message msg : history) {
                boolean isOwn = msg.getSenderId().equals(UserData.currentUser.getUserId());
                if (msg.getType() == Message.MessageType.IMAGE) {
                    displayImage(msg.getContent(), isOwn, null);
                } else {
                    addMessage(msg.getContent(), isOwn, null);
                }
            }
        } else { // GROUP chat
            List<GroupMessage> history = DatabaseHelper.getGroupMessages(currentTargetId);
            for (GroupMessage msg : history) {
                boolean isOwn = msg.getSenderId().equals(UserData.currentUser.getUserId());
                User sender = isOwn ? null : DatabaseHelper.getUserById(msg.getSenderId());
                String senderName = (sender != null) ? sender.getUserName() : null;

                if (msg.getType() == GroupMessage.MessageType.IMAGE) {
                    displayImage(msg.getContent(), isOwn, senderName);
                } else {
                    addMessage(msg.getContent(), isOwn, senderName);
                }
            }
        }
    }

    @Override
    public void onMessageReceived(Message message) {
        if (currentTargetId == null || message.getSenderId() == null) return;

        Platform.runLater(() -> {
            if (currentMode == ChatMode.PRIVATE && message.getSenderId().equals(currentTargetId)) {
                if (message.getType() == Message.MessageType.IMAGE) {
                    displayImage(message.getContent(), false, null);
                } else {
                    addMessage(message.getContent(), false, null);
                }
            } else if (currentMode == ChatMode.GROUP && currentTargetId.equals(message.getReceiverId())) {
                if (message.getSenderId().equals(UserData.currentUser.getUserId())) return;
                User sender = DatabaseHelper.getUserById(message.getSenderId());
                String senderName = (sender != null) ? sender.getUserName() : "Unknown";

                if (message.getType() == Message.MessageType.IMAGE) {
                    displayImage(message.getContent(), false, senderName);
                } else {
                    addMessage(message.getContent(), false, senderName);
                }
            }
        });
    }

    @FXML
    private void sendMessage() {
        String messageText = messageInput.getText().trim();
        if (messageText.isEmpty()) return;

        if (client != null && client.isConnected()) {
            if (currentMode == ChatMode.PRIVATE) {
                Message msgObj = new Message(UserData.currentUser.getUserId(), currentTargetId, messageText, Message.MessageType.TEXT);
                client.sendMessage(msgObj);
                addMessage(messageText, true, null);
            } else {
                GroupMessage groupMsg = new GroupMessage(currentTargetId, UserData.currentUser.getUserId(), messageText, GroupMessage.MessageType.TEXT);
                client.sendGroupMessage(groupMsg);
                addMessage(messageText, true, null);
            }
        } else {
            addSystemMessage("Error: Not connected to server.");
        }
        messageInput.clear();
    }

    // *** متد addMessage با استفاده از TextFlow بازنویسی شد ***
    private void addMessage(String content, boolean isOwnMessage, String senderName) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(isOwnMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 10, 5, 10));

        TextFlow textFlow = new TextFlow();
        textFlow.setMaxWidth(280);

        if (currentMode == ChatMode.GROUP && !isOwnMessage && senderName != null) {
            Text nameText = new Text(senderName + "\n");
            nameText.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            nameText.setFill(Color.CORNFLOWERBLUE);
            textFlow.getChildren().add(nameText);
        }

        Text contentText = new Text(content);
        contentText.setFont(Font.font("Arial", 14));
        contentText.setFill(Color.BLACK);
        textFlow.getChildren().add(contentText);

        StackPane bubble = new StackPane(textFlow);
        String bubbleStyle = isOwnMessage ? "-fx-background-color: #dcf8c6; -fx-background-radius: 10 10 0 10;" : "-fx-background-color: #ffffff; -fx-background-radius: 10 10 10 0;";
        bubble.setStyle(bubbleStyle + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);");
        bubble.setPadding(new Insets(8, 12, 8, 12));

        messageBox.getChildren().add(bubble);
        messagesContainer.getChildren().add(messageBox);
    }

    private void displayImage(String base64Content, boolean isOwnMessage, String senderName) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(isOwnMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 10, 5, 10));

        VBox imageBubble = new VBox(5);
        String bubbleStyle = isOwnMessage ? "-fx-background-color: #dcf8c6; -fx-background-radius: 10 10 0 10;" : "-fx-background-color: #ffffff; -fx-background-radius: 10 10 10 0;";
        imageBubble.setStyle(bubbleStyle + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);");
        imageBubble.setPadding(new Insets(5));

        if (!isOwnMessage && senderName != null) {
            Label senderLabel = new Label(senderName);
            senderLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            senderLabel.setTextFill(Color.CORNFLOWERBLUE);
            VBox.setMargin(senderLabel, new Insets(0, 5, 0, 5));
            imageBubble.getChildren().add(senderLabel);
        }

        byte[] decodedBytes = Base64.getDecoder().decode(base64Content);
        ImageView imageView = new ImageView(new Image(new ByteArrayInputStream(decodedBytes)));
        imageView.setFitWidth(250);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");
        imageBubble.getChildren().add(imageView);

        messageBox.getChildren().add(imageBubble);
        messagesContainer.getChildren().add(messageBox);
    }

    @FXML
    private void handleAttachFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(messageInput.getScene().getWindow());

        if (selectedFile != null) {
            try {
                byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
                String encodedString = Base64.getEncoder().encodeToString(fileContent);

                if (currentMode == ChatMode.PRIVATE) {
                    Message imageMessage = new Message(UserData.currentUser.getUserId(), currentTargetId, encodedString, Message.MessageType.IMAGE);
                    client.sendMessage(imageMessage);
                } else {
                    GroupMessage imageGroupMessage = new GroupMessage(currentTargetId, UserData.currentUser.getUserId(), encodedString, GroupMessage.MessageType.IMAGE);
                    client.sendGroupMessage(imageGroupMessage);
                }
                displayImage(encodedString, true, null);
            } catch (IOException e) {
                e.printStackTrace();
                addSystemMessage("Error: Could not send image.");
            }
        }
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

    @FXML private void handleCall() { System.out.println("Call button clicked"); }
    @FXML private void handleSearch() { System.out.println("Search button clicked"); }
    @FXML private void handleMenu() { System.out.println("Menu button clicked"); }
    @FXML private void handleEmoji() { System.out.println("Emoji button clicked"); }
}
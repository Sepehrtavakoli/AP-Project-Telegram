package org.example.approjectgui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.API.Client;
import org.example.API.ClientManager;
import org.example.database.DatabaseHelper;
import org.example.model.Chanel;
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
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

public class ChatController implements Initializable, Client.MessageListener {

    @FXML private VBox messagesContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField messageInput;
    @FXML private Label chatPartnerName;
    @FXML private Label onlineStatus;

    private Client client;
    private enum ChatMode { PRIVATE, GROUP, CHANNEL }
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
        messageInput.setDisable(false);
        messageInput.setPromptText("Message...");
        Platform.runLater(this::loadChatHistory);
    }

    public void initGroupChat(Group group) {
        this.currentMode = ChatMode.GROUP;
        this.currentTargetId = group.getGroupId();
        this.chatPartnerName.setText(group.getGroupName());
        int memberCount = DatabaseHelper.getGroupMembers(this.currentTargetId).size();
        this.onlineStatus.setText(memberCount + " members");
        this.onlineStatus.setVisible(true);
        messageInput.setDisable(false);
        messageInput.setPromptText("Message...");
        Platform.runLater(this::loadChatHistory);
    }

    public void initChannelChat(Chanel chanel) {
        this.currentMode = ChatMode.CHANNEL;
        this.currentTargetId = chanel.getChanelID();
        this.chatPartnerName.setText(chanel.getChanelName());
        int memberCount = DatabaseHelper.getChanelSubscribers(this.currentTargetId).size();
        this.onlineStatus.setText(memberCount + " subscribers");
        this.onlineStatus.setVisible(true);

        boolean isOwner = chanel.getCreatorID().equals(UserData.currentUser.getUserId());
        if (!isOwner) {
            messageInput.setDisable(true);
            messageInput.setPromptText("Only channel owner can send messages.");
        } else {
            messageInput.setDisable(false);
            messageInput.setPromptText("Message...");
        }

        Platform.runLater(this::loadChatHistory);
    }

    private void loadChatHistory() {
        messagesContainer.getChildren().clear();
        if (UserData.currentUser == null || currentTargetId == null) return;

        switch (currentMode) {
            case PRIVATE:
                List<Message> privateHistory = DatabaseHelper.getPrivateMessages(UserData.currentUser.getUserId(), currentTargetId);
                for (Message msg : privateHistory) {
                    boolean isOwn = msg.getSenderId().equals(UserData.currentUser.getUserId());
                    if (msg.getType() == Message.MessageType.IMAGE) {
                        displayImage(msg.getContent(), isOwn, null, msg.getMessageId());
                    } else {
                        addMessage(msg.getContent(), isOwn, null, msg.getMessageId());
                    }
                }
                break;
            case GROUP:
                List<GroupMessage> groupHistory = DatabaseHelper.getGroupMessages(currentTargetId);
                for (GroupMessage msg : groupHistory) {
                    boolean isOwn = msg.getSenderId().equals(UserData.currentUser.getUserId());
                    User sender = isOwn ? null : DatabaseHelper.getUserById(msg.getSenderId());
                    String senderName = (sender != null) ? sender.getUserName() : null;

                    if (msg.getType() == GroupMessage.MessageType.IMAGE) {
                        displayImage(msg.getContent(), isOwn, senderName, msg.getMessageId());
                    } else {
                        addMessage(msg.getContent(), isOwn, senderName, msg.getMessageId());
                    }
                }
                break;
            case CHANNEL:
                // <<-- این بخش جدید است
                List<Message> channelHistory = DatabaseHelper.getChanelMessages(currentTargetId);
                for (Message msg : channelHistory) {
                    boolean isOwner = msg.getSenderId().equals(UserData.currentUser.getUserId());
                    String senderName = isOwner ? "You" : DatabaseHelper.getUserById(msg.getSenderId()).getUserName();

                    if (msg.getType() == Message.MessageType.IMAGE) {
                        displayImage(msg.getContent(), isOwner, senderName, msg.getMessageId());
                    } else {
                        // در کانال‌ها، معمولاً نام فرستنده در UI نمایش داده می‌شود
                        addMessage(msg.getContent(), isOwner, senderName, msg.getMessageId());
                    }
                }
                break;
        }
    }

    @Override
    public void onMessageReceived(Message message) {
        if (currentTargetId == null || message.getSenderId() == null) return;

        Platform.runLater(() -> {
            if (message.getType() == Message.MessageType.EDIT) {
                String[] parts = message.getContent().split("\\|\\|\\|");
                UUID messageIdToUpdate = UUID.fromString(parts[0]);
                String newContent = parts[1];
                updateMessageInUI(messageIdToUpdate, newContent);
                return;
            }
            if (message.getType() == Message.MessageType.DELETE) {
                UUID messageIdToDelete = UUID.fromString(message.getContent());
                removeMessageFromUI(messageIdToDelete);
                return;
            }

            switch (currentMode) {
                case PRIVATE:
                    if (message.getSenderId().equals(currentTargetId)) {
                        handlePrivateMessage(message, false);
                    }
                    break;
                case GROUP:
                    if (currentTargetId.equals(message.getReceiverId()) && !message.getSenderId().equals(UserData.currentUser.getUserId())) {
                        User sender = DatabaseHelper.getUserById(message.getSenderId());
                        String senderName = (sender != null) ? sender.getUserName() : "Unknown";
                        handleGroupMessage(message, false, senderName);
                    }
                    break;
                case CHANNEL:
                    if (currentTargetId.equals(message.getReceiverId()) && !message.getSenderId().equals(UserData.currentUser.getUserId())) {
                        handleChannelMessage(message, false);
                    }
                    break;
            }
        });
    }

    private void handlePrivateMessage(Message message, boolean isOwn) {
        if (message.getType() == Message.MessageType.IMAGE) {
            displayImage(message.getContent(), isOwn, null, message.getMessageId());
        } else {
            addMessage(message.getContent(), isOwn, null, message.getMessageId());
        }
    }

    private void handleGroupMessage(Message message, boolean isOwn, String senderName) {
        if (message.getType() == Message.MessageType.IMAGE) {
            displayImage(message.getContent(), isOwn, senderName, message.getMessageId());
        } else {
            addMessage(message.getContent(), isOwn, senderName, message.getMessageId());
        }
    }

    private void handleChannelMessage(Message message, boolean isOwn) {
        if (message.getType() == Message.MessageType.IMAGE) {
            displayImage(message.getContent(), isOwn, null, message.getMessageId());
        } else {
            addMessage(message.getContent(), isOwn, null, message.getMessageId());
        }
    }


    @FXML
    private void sendMessage() {
        String messageText = messageInput.getText().trim();
        if (messageText.isEmpty()) return;

        if (client == null || !client.isConnected()) {
            addSystemMessage("Error: Not connected to server.");
            return;
        }

        switch (currentMode) {
            case PRIVATE:
                Message privateMsg = new Message(UserData.currentUser.getUserId(), currentTargetId, messageText, Message.MessageType.TEXT);
                client.sendMessage(privateMsg);
                addMessage(messageText, true, null, privateMsg.getMessageId());
                break;
            case GROUP:
                GroupMessage groupMsg = new GroupMessage(currentTargetId, UserData.currentUser.getUserId(), messageText, GroupMessage.MessageType.TEXT);
                client.sendGroupMessage(groupMsg);
                addMessage(messageText, true, null, groupMsg.getMessageId());
                break;
            case CHANNEL:
                Message channelMsg = new Message(UserData.currentUser.getUserId(), currentTargetId, messageText, Message.MessageType.TEXT);
                client.sendChannelMessage(channelMsg, currentTargetId);
                addMessage(messageText, true, null, channelMsg.getMessageId());
                break;
        }

        messageInput.clear();
    }

    private void addMessage(String content, boolean isOwnMessage, String senderName, UUID messageId) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(isOwnMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 10, 5, 10));
        messageBox.getProperties().put("messageId", messageId);
        messageBox.getProperties().put("messageContent", content);

        TextFlow textFlow = new TextFlow();
        textFlow.setMaxWidth(280);

        // <<-- این بخش برای نمایش نام فرستنده در چت‌های گروهی و کانال‌ها ضروری است
        if (!isOwnMessage && senderName != null) {
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
        String bubbleStyle = isOwnMessage ?
                "-fx-background-color: #dcf8c6; -fx-background-radius: 10 10 0 10;" :
                "-fx-background-color: #ffffff; -fx-background-radius: 10 10 10 0;";
        bubble.setStyle(bubbleStyle + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);");
        bubble.setPadding(new Insets(8, 12, 8, 12));

        if (isOwnMessage) {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem editItem = new MenuItem("Edit");
            MenuItem deleteItem = new MenuItem("Delete");

            editItem.setOnAction(e -> editMessage(messageBox));
            deleteItem.setOnAction(e -> deleteMessage(messageBox));

            contextMenu.getItems().addAll(editItem, deleteItem);
            bubble.setOnContextMenuRequested(e -> contextMenu.show(bubble, e.getScreenX(), e.getScreenY()));
        }

        messageBox.getChildren().add(bubble);
        messagesContainer.getChildren().add(messageBox);
    }

    private void displayImage(String base64Content, boolean isOwnMessage, String senderName, UUID messageId) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(isOwnMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 10, 5, 10));
        messageBox.getProperties().put("messageId", messageId);

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

        if (isOwnMessage) {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(e -> deleteMessage(messageBox));
            contextMenu.getItems().add(deleteItem);
            imageBubble.setOnContextMenuRequested(e -> contextMenu.show(imageBubble, e.getScreenX(), e.getScreenY()));
        }

        messageBox.getChildren().add(imageBubble);
        messagesContainer.getChildren().add(messageBox);
    }

    private void deleteMessage(Node messageNode) {
        UUID messageId = (UUID) messageNode.getProperties().get("messageId");
        if (messageId == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Message");
        alert.setHeaderText("Are you sure you want to delete this message?");
        alert.setContentText("This action cannot be undone.");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            client.sendDeleteRequest(messageId, currentTargetId, currentMode == ChatMode.GROUP);
            removeMessageFromUI(messageId);
        }
    }

    private void removeMessageFromUI(UUID messageId) {
        messagesContainer.getChildren().removeIf(node -> messageId.equals(node.getProperties().get("messageId")));
    }

    private void editMessage(Node messageNode) {
        UUID messageId = (UUID) messageNode.getProperties().get("messageId");
        String currentContent = (String) messageNode.getProperties().get("messageContent");
        if (messageId == null) return;

        TextInputDialog dialog = new TextInputDialog(currentContent);
        dialog.setTitle("Edit Message");
        dialog.setHeaderText("Enter the new text for your message:");
        dialog.setContentText("Message:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newContent -> {
            if (!newContent.trim().isEmpty() && !newContent.equals(currentContent)) {
                client.sendEditRequest(messageId, newContent, currentTargetId, currentMode == ChatMode.GROUP);
                updateMessageInUI(messageId, newContent);
            }
        });
    }

    private void updateMessageInUI(UUID messageId, String newContent) {
        for (Node node : messagesContainer.getChildren()) {
            if (messageId.equals(node.getProperties().get("messageId"))) {
                try {
                    StackPane bubble = (StackPane) ((HBox) node).getChildren().get(0);
                    TextFlow textFlow = (TextFlow) bubble.getChildren().get(0);
                    Text contentText = (Text) textFlow.getChildren().get(textFlow.getChildren().size() - 1);
                    contentText.setText(newContent);
                    node.getProperties().put("messageContent", newContent);
                } catch (Exception e) {
                    System.err.println("Error updating UI for message edit: " + e.getMessage());
                }
                break;
            }
        }
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
                    displayImage(encodedString, true, null, imageMessage.getMessageId());
                } else { // Handles GROUP and CHANNEL
                    // Note: For simplicity, sending images in channels is not implemented on the server side in this version.
                    // This will only work for groups.
                    GroupMessage imageGroupMessage = new GroupMessage(currentTargetId, UserData.currentUser.getUserId(), encodedString, GroupMessage.MessageType.IMAGE);
                    client.sendGroupMessage(imageGroupMessage);
                    displayImage(encodedString, true, null, imageGroupMessage.getMessageId());
                }
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
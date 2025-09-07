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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
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

    @FXML
    private void handleAttachFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(messageInput.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Read file, encode to Base64, and create an IMAGE message.
                byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
                String encodedString = Base64.getEncoder().encodeToString(fileContent);

                Message imageMessage = new Message(UserData.currentUser.getUserId(), currentPartnerId, encodedString, Message.MessageType.IMAGE);
                client.sendMessage(imageMessage); // Use our new flexible send method.

                // Display the sent image in our own chat window immediately.
                Image image = new Image(new ByteArrayInputStream(fileContent));
                addImageMessage(image, true);

            } catch (IOException e) {
                e.printStackTrace();
                addSystemMessage("Error: Could not send image.");
            }
        }
    }


    private void addImageMessage(Image image, boolean isOwnMessage) {
        ImageView imageView = new ImageView(image);
        // Constrain the image size to prevent breaking the layout.
        imageView.setFitWidth(250);
        imageView.setPreserveRatio(true);
        // Add rounded corners to the image view.
        imageView.setStyle("-fx-background-radius: 10; -fx-border-radius: 10;");

        HBox messageBox = new HBox();
        messageBox.setAlignment(isOwnMessage ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        messageBox.setPadding(new Insets(5, 10, 5, 10));

        // Create a bubble effect similar to text messages.
        VBox imageBubble = new VBox(imageView);
        String bubbleStyle = isOwnMessage ?
                "-fx-background-color: #dcf8c6; -fx-background-radius: 10 10 0 10;" :
                "-fx-background-color: #ffffff; -fx-background-radius: 10 10 10 0;";
        imageBubble.setStyle(bubbleStyle + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 3, 0, 0, 1);");
        imageBubble.setPadding(new Insets(5));

        messageBox.getChildren().add(imageBubble);
        messagesContainer.getChildren().add(messageBox);
    }

    @Override
    public void onMessageReceived(Message message) {
        if (message.getType() == Message.MessageType.SYSTEM) {
            addSystemMessage(message.getContent());
            return;
        }

        if (currentPartnerId != null && message.getSenderId() != null && message.getSenderId().equals(currentPartnerId)) {
            // Check if the message is a TEXT or an IMAGE and handle accordingly.
            if (message.getType() == Message.MessageType.TEXT) {
                String timestamp = message.getTimestamp();
                String content = message.getContent();
                String displayMessage = "[" + timestamp + "] " + this.currentPartnerName + ": " + content;
                addMessage(displayMessage, false);
            } else if (message.getType() == Message.MessageType.IMAGE) {
                // Decode the Base64 string back to an image and display it.
                byte[] decodedBytes = Base64.getDecoder().decode(message.getContent());
                Image image = new Image(new ByteArrayInputStream(decodedBytes));
                addImageMessage(image, false);
            }
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

                // Check the type of message from history.
                if (msg.getType() == Message.MessageType.TEXT) {
                    String senderPrefix = isOwn ? "You: " : this.currentPartnerName + ": ";
                    String displayMessage = "[" + msg.getTimestamp() + "] " + senderPrefix + msg.getContent();
                    addMessage(displayMessage, isOwn);
                } else if (msg.getType() == Message.MessageType.IMAGE) {
                    byte[] decodedBytes = Base64.getDecoder().decode(msg.getContent());
                    Image image = new Image(new ByteArrayInputStream(decodedBytes));
                    addImageMessage(image, isOwn);
                }
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
    @FXML private void handleEmoji() { System.out.println("Emoji button clicked"); }
}
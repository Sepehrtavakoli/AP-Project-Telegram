package org.example.approjectgui;

import com.google.gson.Gson;
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
import org.example.database.DatabaseHelper;
import org.example.model.User;
import org.example.projectbackend.Message;
import org.example.util.MessageUtils;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatController implements Initializable {

    @FXML private VBox messagesContainer;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField messageInput;
    @FXML private Label chatPartnerName;
    @FXML private Label onlineStatus;
    @FXML private Circle onlineIndicator;

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
        connectToServer();
        loadChatHistory();

        messagesContainer.heightProperty().addListener((obs, oldVal, newVal) ->
                Platform.runLater(() -> scrollPane.setVvalue(1.0)));

        messageInput.setOnAction(event -> sendMessage());
        Platform.runLater(() -> messageInput.requestFocus());
        scrollPane.setFitToWidth(true);
        messagesContainer.setFillWidth(true);
    }

    private void setupChatUI() {
        chatPartnerName.setText(partnerName);
        onlineStatus.setText("online");
        onlineIndicator.setVisible(true);
    }

    private String getPartnerName(UUID partnerId) {
        return currentPartnerName != null ? currentPartnerName : "User";
    }

    private void connectToServer() {
        User currentUser = UserData.currentUser;
        if (currentUser != null) {
            client = new Client();
            client.setMessageListener(message ->
                    Platform.runLater(() -> processIncomingMessage(message))
            );

            boolean connected = client.connectToServer("localhost", 1234, currentUser);
            if (connected) {
                System.out.println("Connected to server successfully as: " + currentUser.getUserName());
                addSystemMessage("Connected to server");
            } else {
                System.err.println("Failed to connect to server");
                addSystemMessage("Failed to connect to server");
            }
        }
    }

    private void processIncomingMessage(String message) {
        try {
            System.out.println("Received message: " + message);

            if (MessageUtils.isJsonMessage(message)) {
                Message messageObj = MessageUtils.parseJsonMessage(message);

                if (messageObj != null) {
                    System.out.println("Parsed message - Sender: " + messageObj.getSenderId() + ", Receiver: " + messageObj.getReceiverId());
                    System.out.println("Current partner: " + currentPartnerId);
                    System.out.println("Current user: " + (UserData.currentUser != null ? UserData.currentUser.getUserId() : "null"));

                    // بررسی اینکه پیام مربوط به چت فعلی است
                    boolean isForCurrentChat = currentPartnerId != null &&
                            messageObj.getSenderId().equals(currentPartnerId) &&
                            messageObj.getReceiverId().equals(UserData.currentUser.getUserId());

                    System.out.println("Is for current chat: " + isForCurrentChat);

                    if (isForCurrentChat) {
                        // نمایش پیام دریافتی
                        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                        String displayMessage = "[" + timestamp + "] " + currentPartnerName + ": " + messageObj.getContent();
                        addMessage(displayMessage, false);

                        // ذخیره پیام در دیتابیس
                        DatabaseHelper.savePrivateMessage(messageObj);
                    } else {
                        System.out.println("Message not for current chat. Ignoring.");
                    }
                }
            }
            else if (MessageUtils.isSystemMessage(message)) {
                // پیام سیستم است
                addSystemMessage(message);
            }
            else {
                // پیام متنی معمولی
                addMessage(message, false);
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
        }
    }

    @FXML
    private void sendMessage() {
        String messageText = messageInput.getText().trim();
        if (!messageText.isEmpty() && currentPartnerId != null) {
            // ایجاد پیام جدید
            Message message = new Message(
                    UserData.currentUser.getUserId(),
                    currentPartnerId,
                    messageText,
                    Message.MessageType.TEXT
            );

            // تبدیل به JSON و ارسال
            String jsonMessage = MessageUtils.toJsonMessage(message);
            if (jsonMessage != null && client != null) {
                client.sendMessage(jsonMessage);
            }

            // نمایش پیام در رابط کاربری فرستنده
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            String messageWithTime = "[" + timestamp + "] You: " + messageText;
            addMessage(messageWithTime, true);

            // ذخیره پیام در دیتابیس
            DatabaseHelper.savePrivateMessage(message);

            messageInput.clear();
        }
    }

    // اضافه کردن متد برای رفرش Homepage
    private void refreshHomePage() {
        try {
            // پیدا کردن Stage مربوط به Homepage
            Stage homeStage = findHomeStage();
            if (homeStage != null) {
                // پیدا کردن کنترلر Homepage و رفرش لیست چت‌ها
                Scene homeScene = homeStage.getScene();
                if (homeScene != null && homeScene.getRoot() != null) {
                    homeScene.getRoot().setUserData("refresh_needed");
                }
            }
        } catch (Exception e) {
            System.err.println("Error refreshing home page: " + e.getMessage());
        }
    }

    private Stage findHomeStage() {
        // پیدا کردن Stage مربوط به Homepage از بین پنجره‌های باز
        for (Stage stage : Stage.getWindows().stream()
                .filter(window -> window instanceof Stage)
                .map(window -> (Stage) window)
                .collect(Collectors.toList())) {

            if (stage.getScene() != null && stage.getScene().getRoot() != null) {
                Parent root = stage.getScene().getRoot();
                // بررسی اینکه آیا این پنجره مربوط به Homepage است
                if (root.lookup("#chatsList") != null) {
                    return stage;
                }
            }
        }
        return null;
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

    @FXML
    private void handleBack() {
        try {
            if (client != null) {
                client.closeEverything();
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/approjectgui/HomePage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) messageInput.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            System.err.println("Error loading HomePage: " + e.getMessage());
            e.printStackTrace();
            Stage stage = (Stage) messageInput.getScene().getWindow();
            stage.close();
        }
    }

    private void loadChatHistory() {
        if (UserData.currentUser != null && currentPartnerId != null) {
            List<Message> history = DatabaseHelper.getPrivateMessages(
                    UserData.currentUser.getUserId(),
                    currentPartnerId
            );

            for (Message message : history) {
                boolean isOwn = message.getSenderId().equals(UserData.currentUser.getUserId());
                String senderName = isOwn ? "You" : getPartnerName(message.getSenderId());
                String displayText = "[" + message.getTimestamp() + "] " + senderName + ": " + message.getContent();
                addMessage(displayText, isOwn);
            }
        }
    }

    public void setPartner(String partnerName, UUID partnerId) {
        this.currentPartnerName = partnerName;
        this.currentPartnerId = partnerId;
        setPartnerName(partnerName);
        loadChatHistory(); // بارگذاری تاریخچه هنگام تنظیم پارتنر
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

    // دکمه‌های منو
    @FXML private void handleCall() { System.out.println("Call button clicked"); }
    @FXML private void handleSearch() { System.out.println("Search button clicked"); }
    @FXML private void handleMenu() { System.out.println("Menu button clicked"); }
    @FXML private void handleAttachFile() { System.out.println("Attach file button clicked"); }
    @FXML private void handleEmoji() { System.out.println("Emoji button clicked"); }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setPartnerName(String name) {
        this.partnerName = name;
        chatPartnerName.setText(name);
    }
}

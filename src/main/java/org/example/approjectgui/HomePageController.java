package org.example.approjectgui;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.database.DatabaseHelper;
import org.example.model.User;
import org.example.projectbackend.Message;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class HomePageController implements Initializable {

    @FXML private ImageView userAvatar;
    @FXML private Label userNameLabel;
    @FXML private VBox chatsList;
    @FXML private ImageView MenuLines;
    @FXML private AnchorPane sideMenu;
    @FXML private Pane overlayPane;
    @FXML private Label menuAccountName;
    @FXML private ImageView menuAvatar;
    @FXML private ImageView newChatButton;
    @FXML private Label StartMessage;

    private boolean menuVisible = false;
    private Map<String, UUID> contactUserIdMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (UserData.firstName != null && !UserData.firstName.isEmpty()) {
            setAccountName(UserData.firstName);
        } else {
            setAccountName("کاربر");
        }

        setupRealChats(); // جایگزین setupSampleChats
        setupAvatar();
        initializeMenu();

        // ایجاد نقشه برای ارتباط نام مخاطب با userId
        setupContactUserIdMap();
    }

    private void setupContactUserIdMap() {
        if (UserData.currentUser != null) {
            List<org.example.projectbackend.Contact> contacts = DatabaseHelper.getContactsByUserId(UserData.currentUser.getUserId());
            for (org.example.projectbackend.Contact contact : contacts) {
                if (contact.getContactUser() != null) {
                    String fullName = contact.getFirstName() + (contact.getLastName() != null ? " " + contact.getLastName() : "");
                    contactUserIdMap.put(fullName, contact.getContactUser().getUserId());
                }
            }
        }
    }

    private void initializeMenu() {
        if (sideMenu != null) {
            sideMenu.setVisible(false);
            sideMenu.setTranslateX(-300);
        }
        if (overlayPane != null) {
            overlayPane.setVisible(false);
            overlayPane.setOpacity(0);
        }
    }

    private void setupAvatar() {
        if (userAvatar != null) {
            Circle clip = new Circle(22.5, 22.5, 22.5);
            userAvatar.setClip(clip);
        }
        if (menuAvatar != null) {
            Circle clip = new Circle(35, 35, 35);
            menuAvatar.setClip(clip);
        }
    }

    private void setupRealChats() {
        if (chatsList != null) {
            chatsList.getChildren().clear();

            if (UserData.currentUser != null) {
                // دریافت مخاطبان کاربر
                List<org.example.projectbackend.Contact> contacts = DatabaseHelper.getContactsByUserId(UserData.currentUser.getUserId());

                if (contacts.isEmpty()) {
                    addNoChatsMessage();
                } else {
                    // برای هر مخاطب، یک آیتم چت ایجاد کنید
                    for (org.example.projectbackend.Contact contact : contacts) {
                        // بررسی کنید که contactUser null نباشد
                        if (contact.getContactUser() == null) {
                            System.out.println("Warning: Contact user is null for contact: " + contact.getFirstName());
                            continue; // از این مخاطب صرف نظر کنید
                        }

                        // دریافت آخرین پیام با این مخاطب
                        List<Message> messages = DatabaseHelper.getPrivateMessages(
                                UserData.currentUser.getUserId(),
                                contact.getContactUser().getUserId()
                        );

                        String lastMessage = "";
                        String lastMessageTime = "";

                        if (!messages.isEmpty()) {
                            // مرتب کردن پیام‌ها بر اساس زمان
                            messages.sort((m1, m2) -> m2.getTimestamp().compareTo(m1.getTimestamp()));
                            Message lastMsg = messages.get(0);
                            lastMessage = lastMsg.getContent();
                            lastMessageTime = formatMessageTime(lastMsg.getTimestamp());
                        }

                        String fullName = contact.getFirstName() + (contact.getLastName() != null ? " " + contact.getLastName() : "");
                        addChatItem(fullName, lastMessage, lastMessageTime, "");
                    }
                }
            } else {
                addNoChatsMessage();
            }
        }
    }

    private void addNoChatsMessage() {
        Label noChatsLabel = new Label("No chats yet. Start a conversation!");
        noChatsLabel.setFont(Font.font("Arial", 14));
        noChatsLabel.setTextFill(Color.GRAY);
        noChatsLabel.setPadding(new Insets(20));
        noChatsLabel.setAlignment(Pos.CENTER);
        chatsList.getChildren().add(noChatsLabel);
    }

    private String formatMessageTime(String timestamp) {
        try {
            LocalDateTime time = LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime now = LocalDateTime.now();

            if (time.toLocalDate().equals(now.toLocalDate())) {
                return time.format(DateTimeFormatter.ofPattern("HH:mm"));
            } else if (time.toLocalDate().equals(now.toLocalDate().minusDays(1))) {
                return "Yesterday";
            } else {
                return time.format(DateTimeFormatter.ofPattern("MM/dd"));
            }
        } catch (Exception e) {
            return timestamp;
        }
    }

    private void addChatItem(String name, String lastMessage, String time, String unreadCount) {
        HBox chatItem = new HBox();
        chatItem.setAlignment(Pos.CENTER_LEFT);
        chatItem.setStyle("-fx-padding: 15; -fx-cursor: hand;");
        chatItem.setOnMouseEntered(e -> chatItem.setStyle("-fx-background-color: #3d4354; -fx-padding: 15; -fx-cursor: hand;"));
        chatItem.setOnMouseExited(e -> chatItem.setStyle("-fx-background-color: transparent; -fx-padding: 15; -fx-cursor: hand;"));

        // ذخیره نام مخاطب به عنوان ویژگی آیتم
        chatItem.setUserData(name);

        Label avatarLabel = new Label(name.substring(0, 1).toUpperCase());
        avatarLabel.setFont(Font.font("Arial Bold", 16));
        avatarLabel.setTextFill(Color.WHITE);
        avatarLabel.setStyle("-fx-background-color: #0088cc; -fx-background-radius: 22.5; -fx-min-width: 45; -fx-min-height: 45; -fx-alignment: center;");

        VBox textBox = new VBox();
        textBox.setSpacing(3.0);
        textBox.setPadding(new Insets(0, 0, 0, 12));
        textBox.setPrefWidth(180.0);

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Arial", 14));
        nameLabel.setTextFill(Color.WHITE);

        Label messageLabel = new Label(lastMessage);
        messageLabel.setFont(Font.font("Arial", 12));
        messageLabel.setTextFill(Color.LIGHTGRAY);
        messageLabel.setMaxWidth(180.0);
        messageLabel.setWrapText(true);

        textBox.getChildren().addAll(nameLabel, messageLabel);

        VBox rightBox = new VBox();
        rightBox.setAlignment(Pos.TOP_RIGHT);
        rightBox.setSpacing(5.0);

        Label timeLabel = new Label(time);
        timeLabel.setFont(Font.font("Arial", 11));
        timeLabel.setTextFill(Color.LIGHTGRAY);

        if (!unreadCount.isEmpty()) {
            Label unreadLabel = new Label(unreadCount);
            unreadLabel.setFont(Font.font("Arial Bold", 11));
            unreadLabel.setTextFill(Color.WHITE);
            unreadLabel.setStyle("-fx-background-color: #0088cc; -fx-background-radius: 10; -fx-padding: 3 8 3 8;");
            unreadLabel.setAlignment(Pos.CENTER);
            rightBox.getChildren().addAll(timeLabel, unreadLabel);
        } else {
            rightBox.getChildren().add(timeLabel);
        }

        chatItem.getChildren().addAll(avatarLabel, textBox, rightBox);
        chatsList.getChildren().add(chatItem);

        // اضافه کردن هندلر کلیک
        chatItem.setOnMouseClicked(this::handleChatItemClick);
    }

    @FXML
    private void handleChatItemClick(MouseEvent event) {
        try {
            HBox clickedItem = (HBox) event.getSource();
            String partnerName = (String) clickedItem.getUserData();

            // پیدا کردن userId از نقشه
            UUID partnerId = contactUserIdMap.get(partnerName);

            if (partnerId != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatPage.fxml"));
                Parent root = loader.load();

                ChatController controller = loader.getController();
                controller.setPartner(partnerName, partnerId);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } else {
                System.out.println("User ID not found for: " + partnerName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // متد برای رفرش لیست چت‌ها
    public void refreshChatsList() {
        setupRealChats();
    }

    // بقیه متدها بدون تغییر...
    @FXML
    private void SwitchToMenu(MouseEvent event) {
        if (menuVisible) {
            closeMenu();
        } else {
            openMenu();
        }
    }

    private void openMenu() {
        if (sideMenu == null || overlayPane == null) return;

        sideMenu.setVisible(true);
        overlayPane.setVisible(true);

        TranslateTransition slide = new TranslateTransition(Duration.millis(300), sideMenu);
        slide.setToX(0);

        FadeTransition fade = new FadeTransition(Duration.millis(300), overlayPane);
        fade.setToValue(0.4);

        ParallelTransition parallel = new ParallelTransition(slide, fade);
        parallel.play();

        menuVisible = true;
    }

    private void closeMenu() {
        if (sideMenu == null || overlayPane == null) return;

        TranslateTransition slide = new TranslateTransition(Duration.millis(300), sideMenu);
        slide.setToX(-300);

        FadeTransition fade = new FadeTransition(Duration.millis(300), overlayPane);
        fade.setToValue(0);

        ParallelTransition parallel = new ParallelTransition(slide, fade);
        parallel.setOnFinished(e -> {
            sideMenu.setVisible(false);
            overlayPane.setVisible(false);
        });
        parallel.play();

        menuVisible = false;
    }



    @FXML
    private void closeMenu(MouseEvent event) {
        closeMenu();
    }

    @FXML
    private void highlightMenuItem(MouseEvent event) {
        Node source = (Node) event.getSource();
        source.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-cursor: hand;");
    }

    @FXML
    private void unhighlightMenuItem(MouseEvent event) {
        Node source = (Node) event.getSource();
        source.setStyle("-fx-background-color: transparent; -fx-padding: 15; -fx-cursor: hand;");
    }

    @FXML
    private void handleMenuItem(MouseEvent event) {
        Node source = (Node) event.getSource();
        System.out.println("Menu item clicked: " + source.getId());
        closeMenu();
    }

    @FXML
    private void handleLogout(MouseEvent event) {
        System.out.println("Logout clicked");
        closeMenu();
    }

    @FXML
    private void SwitchToContactPage(MouseEvent mouseEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ContactPage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) newChatButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading ContactPage: " + e.getMessage());
        }
    }

    public void setAccountName(String name) {
        if (userNameLabel != null) userNameLabel.setText(name);
        if (menuAccountName != null) menuAccountName.setText(name);
    }

    public void setFirstName(String firstName) {
        UserData.firstName = firstName;
        setAccountName(firstName);
    }
}
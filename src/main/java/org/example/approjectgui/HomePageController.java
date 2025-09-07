package org.example.approjectgui;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
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
import org.example.API.Client;
import org.example.API.ClientManager;
import org.example.database.DatabaseHelper;
import org.example.model.User;
import org.example.projectbackend.Message; // Import the correct Message class
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import org.example.model.Group;
import java.util.ArrayList;


public class HomePageController implements Initializable, Client.MessageListener {

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
    @FXML private Label PhoneNumberShow;

    private boolean menuVisible = false;
    private Client client;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (UserData.currentUser != null) {
            setAccountName(UserData.currentUser.getFirstName());
            setPhoneNumber(UserData.currentUser.getPhoneNumber());
        } else {
            setAccountName("User");
        }

        this.client = ClientManager.getInstance();
        if (this.client != null) {
            this.client.addMessageListener(this);
        }

        setupRealChats();
        loadGroups();
        setupAvatar();
        initializeMenu();
    }

    @Override
    public void onMessageReceived(Message message) {
        System.out.println("HomePage received a message from: " + message.getSenderId());
        // --- مرحله ۲: نمایش نشانگر هنگام دریافت پیام ---
        Platform.runLater(() -> {
            // لیست چت‌ها را برای آپدیت آخرین پیام رفرش می‌کنیم
            refreshChatList();

            // آیتم چت مربوط به فرستنده پیام را پیدا می‌کنیم
            for (Node node : chatsList.getChildren()) {
                if (node instanceof HBox) {
                    HBox chatItem = (HBox) node;
                    UUID chatUserId = (UUID) chatItem.getProperties().get("userId");

                    // اگر ID فرستنده با ID این آیتم چت یکی بود
                    if (chatUserId != null && chatUserId.equals(message.getSenderId())) {
                        Circle indicator = (Circle) chatItem.getProperties().get("newMessageIndicator");
                        if (indicator != null) {
                            indicator.setVisible(true); // نشانگر را روشن کن
                        }
                        break; // از حلقه خارج شو
                    }
                }
            }
        });
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
        if (chatsList != null && UserData.currentUser != null) {
            chatsList.getChildren().clear();
            // <<-- استفاده از متد جدید و بهینه برای گرفتن لیست کامل چت‌ها
            List<User> chatList = DatabaseHelper.getChatListUsers(UserData.currentUser.getUserId());

            if (chatList.isEmpty()) {
                Label noUsersLabel = new Label("No chats available");
                noUsersLabel.setTextFill(Color.GRAY);
                noUsersLabel.setFont(Font.font("Arial", 14));
                chatsList.getChildren().add(noUsersLabel);
            } else {
                for (User contactUser : chatList) {
                    Message lastMessage = DatabaseHelper.getLastMessage(UserData.currentUser.getUserId(), contactUser.getUserId());

                    String previewText = "No messages yet";
                    if (lastMessage != null) {
                        String senderPrefix = lastMessage.getSenderId().equals(UserData.currentUser.getUserId()) ? "You: " : "";

                        if (lastMessage.getType() == Message.MessageType.IMAGE) {
                            previewText = senderPrefix + "📷 Image";
                        } else {
                            previewText = senderPrefix + lastMessage.getContent();
                        }
                    }

                    if (previewText.length() > 25 && lastMessage != null && lastMessage.getType() == Message.MessageType.TEXT) {
                        previewText = previewText.substring(0, 22) + "...";
                    }

                    addChatItem(contactUser.getUserName(), previewText, "", contactUser.getUserId());
                }
            }
        }
    }

    private void addChatItem(String name, String lastMessage, String time, UUID userId) {
        HBox chatItem = new HBox();
        chatItem.setAlignment(Pos.CENTER_LEFT);
        chatItem.setStyle("-fx-padding: 15; -fx-cursor: hand;");
        chatItem.setOnMouseEntered(e -> chatItem.setStyle("-fx-background-color: #3d4354; -fx-padding: 15; -fx-cursor: hand;"));
        chatItem.setOnMouseExited(e -> chatItem.setStyle("-fx-background-color: transparent; -fx-padding: 15; -fx-cursor: hand;"));

        chatItem.getProperties().put("userId", userId);

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
        rightBox.setAlignment(Pos.CENTER_RIGHT); // <<-- تراز وسط برای زیبایی بیشتر
        rightBox.setSpacing(8.0); // <<-- فاصله بین زمان و نقطه سبز

        Label timeLabel = new Label(time);
        timeLabel.setFont(Font.font("Arial", 11));
        timeLabel.setTextFill(Color.LIGHTGRAY);

        // --- مرحله ۱: ایجاد نقطه سبز (نشانگر پیام جدید) ---
        Circle newMessageIndicator = new Circle(5, Color.LIMEGREEN);
        newMessageIndicator.setVisible(false); // در ابتدا مخفی است
        // ---------------------------------------------------

        rightBox.getChildren().addAll(timeLabel, newMessageIndicator);

        // <<-- نشانگر را در properties ذخیره می‌کنیم تا بعداً به آن دسترسی داشته باشیم
        chatItem.getProperties().put("newMessageIndicator", newMessageIndicator);

        chatItem.getChildren().addAll(avatarLabel, textBox, rightBox);
        chatItem.setOnMouseClicked(this::handleChatItemClick);

        chatsList.getChildren().add(chatItem);
    }

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
    private void SwitchToContactPage(MouseEvent mouseEvent) {
        try {
            if (this.client != null) {
                this.client.removeMessageListener(this);
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ContactPage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) newChatButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToSettingsPage(MouseEvent event) {
        try {
            // Un-register as a listener before leaving the page
            if (this.client != null) {
                this.client.removeMessageListener(this);
            }

            Parent root = FXMLLoader.load(getClass().getResource("SettingsPage.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleChatItemClick(MouseEvent event) {
        HBox chatItem = (HBox) event.getSource();

        // --- مرحله ۳: پنهان کردن نشانگر هنگام کلیک ---
        Circle indicator = (Circle) chatItem.getProperties().get("newMessageIndicator");
        if (indicator != null) {
            indicator.setVisible(false);
        }
        // ---------------------------------------------

        try {
            UUID partnerId = (UUID) chatItem.getProperties().get("userId");
            String partnerName = getPartnerNameFromChatItem(chatItem);

            if (partnerId != null) {
                if (this.client != null) {
                    this.client.removeMessageListener(this);
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatPage.fxml"));
                Parent root = loader.load();

                ChatController controller = loader.getController();
                controller.setPartner(partnerName, partnerId);

                Stage stage = (Stage) chatItem.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getPartnerNameFromChatItem(HBox chatItem) {
        try {
            VBox textBox = (VBox) chatItem.getChildren().get(1);
            Label nameLabel = (Label) textBox.getChildren().get(0);
            return nameLabel.getText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "User";
    }

    @FXML
    private void handleMenuItem(MouseEvent event) {
        Node source = (Node) event.getSource();
        // You can add navigation for other items here later
        System.out.println("Menu item clicked: " + source.getId());
        closeMenu();
    }

    public void setAccountName(String name) {
        if (userNameLabel != null) userNameLabel.setText(name);
        if (menuAccountName != null) menuAccountName.setText(name);
    }

    public void setPhoneNumber(String phoneNumber) {
        if (PhoneNumberShow != null) PhoneNumberShow.setText(phoneNumber);
    }

    public void refreshChatList() {
        setupRealChats();
    }

    private void openGroupChat(Group group) {
        System.out.println("Opening group chat: " + group.getGroupName());

        try {
            if (this.client != null) {
                this.client.removeMessageListener(this);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("GroupChatPage.fxml"));
            Parent root = loader.load();

            GroupChatController controller = loader.getController();
            controller.setGroup(group);

            Stage stage = (Stage) chatsList.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error opening group chat: " + e.getMessage());
        }
    }

    // این متد رو به کلاس HomePageController اضافه کنید:
    private void loadGroups() {
        if (UserData.currentUser != null) {
            List<Group> userGroups = DatabaseHelper.getGroupsForUser(UserData.currentUser.getUserId());

            if (userGroups != null && !userGroups.isEmpty()) {
                for (Group group : userGroups) {
                    addGroupItem(group);
                }
            }
        }
    }

    // این متد رو به کلاس HomePageController اضافه کنید:
    private void addGroupItem(Group group) {
        HBox groupItem = new HBox();
        groupItem.setAlignment(Pos.CENTER_LEFT);
        groupItem.setStyle("-fx-padding: 15; -fx-cursor: hand;");
        groupItem.setOnMouseEntered(e -> groupItem.setStyle("-fx-background-color: #3d4354; -fx-padding: 15; -fx-cursor: hand;"));
        groupItem.setOnMouseExited(e -> groupItem.setStyle("-fx-background-color: transparent; -fx-padding: 15; -fx-cursor: hand;"));

        groupItem.getProperties().put("groupId", group.getGroupId());
        groupItem.getProperties().put("isGroup", true); // علامت گذاری که این یک گروه است

        groupItem.setOnMouseClicked(e -> {
            openGroupChat(group);
        });

        // آواتار گروه (حرف اول نام گروه)
        String avatarText = group.getGroupName().substring(0, 1).toUpperCase();
        Label avatarLabel = new Label(avatarText);
        avatarLabel.setFont(Font.font("Arial Bold", 16));
        avatarLabel.setTextFill(Color.WHITE);
        avatarLabel.setStyle("-fx-background-color: #25D366; -fx-background-radius: 22.5; -fx-min-width: 45; -fx-min-height: 45; -fx-alignment: center;");

        VBox textBox = new VBox();
        textBox.setSpacing(3.0);
        textBox.setPadding(new Insets(0, 0, 0, 12));
        textBox.setPrefWidth(180.0);

        Label nameLabel = new Label(group.getGroupName());
        nameLabel.setFont(Font.font("Arial", 14));
        nameLabel.setTextFill(Color.WHITE);

        Label typeLabel = new Label("Group");
        typeLabel.setFont(Font.font("Arial", 12));
        typeLabel.setTextFill(Color.LIGHTGRAY);

        textBox.getChildren().addAll(nameLabel, typeLabel);

        VBox rightBox = new VBox();
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        rightBox.setSpacing(8.0);

        // نقطه سبز برای پیام‌های جدید (اختیاری)
        Circle newMessageIndicator = new Circle(5, Color.LIMEGREEN);
        newMessageIndicator.setVisible(false);
        rightBox.getChildren().add(newMessageIndicator);

        groupItem.getProperties().put("newMessageIndicator", newMessageIndicator);
        groupItem.getChildren().addAll(avatarLabel, textBox, rightBox);

        chatsList.getChildren().add(groupItem);
    }



    @FXML private void handleLogout(MouseEvent event) {}
    @FXML private void unhighlightMenuItem(MouseEvent event) {}
    @FXML private void highlightMenuItem(MouseEvent event) {}
}
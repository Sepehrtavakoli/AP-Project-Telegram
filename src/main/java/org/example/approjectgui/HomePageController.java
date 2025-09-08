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
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
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
import org.example.API.Server;
import org.example.database.DatabaseHelper;
import org.example.model.Chanel;
import org.example.model.User;
import org.example.projectbackend.Message; // Import the correct Message class
import java.io.IOException;
import java.net.URL;
import java.util.*;

import org.example.model.Group;

import static org.example.approjectgui.AppSceneController.switchToLogin;


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
    @FXML private TextField searchField;

    private boolean menuVisible = false;
    private Client client;
    private List<Node> allChatItems = new ArrayList<>();

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

        // فراخوانی تنها متد یکپارچه برای بارگذاری همه چت‌ها
        populateChatList();

        // متدهای قدیمی که باعث خطا می‌شدند، حذف شدند:
        // loadGroups();
        // loadChanels();

        setupAvatar();
        initializeMenu();
//        loadChanels();
        allChatItems.addAll(chatsList.getChildren());
    }

    @FXML
    private void handleSearchKeyReleased(KeyEvent event) {
        filterChatsList();
    }

    private void filterChatsList() {
        String searchText = searchField.getText().toLowerCase().trim();

        chatsList.getChildren().clear(); // Clear the current list

        if (searchText.isEmpty()) {
            // If search is empty, show all chats
            chatsList.getChildren().addAll(allChatItems);
        } else {
            // Filter items whose name starts with the search text
            for (Node item : allChatItems) {
                if (item instanceof HBox) {
                    HBox chatItem = (HBox) item;
                    try {
                        // Extract the name label from the HBox structure
                        // Assumes structure: [Avatar Label, VBox (Name Label, Message Label), VBox (Time, Indicator)]
                        VBox textVBox = (VBox) chatItem.getChildren().get(1);
                        Label nameLabel = (Label) textVBox.getChildren().get(0);
                        String chatName = nameLabel.getText().toLowerCase();

                        if (chatName.startsWith(searchText)) {
                            chatsList.getChildren().add(item);
                        }
                    } catch (IndexOutOfBoundsException | ClassCastException e) {
                        System.err.println("Error extracting name from chat item for filtering: " + e.getMessage());
                        // Skip this item if structure is unexpected
                    }
                }
            }
            // Sort the filtered results alphabetically by name
            chatsList.getChildren().sort(Comparator.comparing(node -> {
                if (node instanceof HBox) {
                    HBox chatItem = (HBox) node;
                    try {
                        VBox textVBox = (VBox) chatItem.getChildren().get(1);
                        Label nameLabel = (Label) textVBox.getChildren().get(0);
                        return nameLabel.getText().toLowerCase();
                    } catch (Exception e) {
                        return ""; // In case of error, place at the beginning
                    }
                }
                return "";
            }));
        }
    }

    @Override
    public void onMessageReceived(Message message) {
        Platform.runLater(() -> {
            // این بخش را برای مدیریت نوتیفیکیشن کانال بهینه می‌کنیم.

            // اگر پیام از نوع EDIT یا DELETE بود، نیازی به نوتیفیکیشن ندارد.
            if (message.getType() == Message.MessageType.EDIT || message.getType() == Message.MessageType.DELETE) {
                return;
            }

            // بررسی می‌کنیم که آیا پیام مربوط به کانال‌ها است.
            // بر اساس طراحی فعلی، پیام‌های کانال در سمت کلاینت receiverId ندارند.
            // این یک مشکل در معماری است. برای حل بهینه، از این خط استفاده نمی‌کنیم.
            // در عوض، به سراغ logic پیام‌های گروهی و خصوصی می‌رویم.

            // این حلقه به درستی تمام آیتم‌های لیست را بررسی می‌کند.
            for (Node node : chatsList.getChildren()) {
                if (node instanceof HBox) {
                    HBox chatItem = (HBox) node;

                    // 1. بررسی برای پیام گروهی
                    if (chatItem.getProperties().containsKey("isGroup")) {
                        UUID itemGroupId = (UUID) chatItem.getProperties().get("groupId");
                        if (itemGroupId != null && itemGroupId.equals(message.getReceiverId())) {
                            Circle indicator = (Circle) chatItem.getProperties().get("newMessageIndicator");
                            if (indicator != null) {
                                indicator.setVisible(true);
                            }
                            break;
                        }
                    }
                    // 2. بررسی برای پیام خصوصی
                    else if (chatItem.getProperties().containsKey("userId")) {
                        UUID itemUserId = (UUID) chatItem.getProperties().get("userId");
                        if (itemUserId != null && itemUserId.equals(message.getSenderId())) {
                            Circle indicator = (Circle) chatItem.getProperties().get("newMessageIndicator");
                            if (indicator != null) {
                                indicator.setVisible(true);
                            }
                            break;
                        }
                    }
                    // 3. بررسی برای پیام کانال
                    // این بخش به دلیل مشکلات معماری فعلی (عدم وجود receiverId در پیام کانال)
                    // به درستی کار نمی‌کند و نیاز به تغییرات در Client.java و ClientHandler.java دارد.
                    // در مرحله بعد به این مشکل می‌پردازیم.
                }
            }

            // نمایش فوری آخرین پیام در لیست چت‌ها
            refreshChatList();
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

        // پنهان کردن نشانگر نوتیفیکیشن
        Circle indicator = (Circle) chatItem.getProperties().get("newMessageIndicator");
        if (indicator != null) indicator.setVisible(false);

        try {
            if (this.client != null) this.client.removeMessageListener(this);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatPage.fxml"));
            Parent root = loader.load();
            ChatController controller = loader.getController();

            // 1. بررسی آیا آیتم یک کانال است
            if (chatItem.getProperties().containsKey("isChanel")) {
                UUID chanelId = (UUID) chatItem.getProperties().get("chanelId");
                Chanel chanel = DatabaseHelper.getChanelById(chanelId);
                if (chanel != null) {
                    controller.initChannelChat(chanel);
                }
            }
            // 2. بررسی آیا آیتم یک گروه است
            else if (chatItem.getProperties().containsKey("isGroup")) {
                UUID groupId = (UUID) chatItem.getProperties().get("groupId");
                Group group = DatabaseHelper.getGroupById(groupId);
                if (group != null) {
                    controller.initGroupChat(group);
                }
            }
            // 3. اگر هیچ کدام نبود، پس یک چت خصوصی است
            else {
                UUID partnerId = (UUID) chatItem.getProperties().get("userId");
                User partnerUser = DatabaseHelper.getUserById(partnerId);
                if (partnerUser != null) {
                    controller.initPrivateChat(partnerUser);
                }
            }

            Stage stage = (Stage) chatItem.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

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
        // <<-- این متد هم باید از منطق کامل بارگذاری استفاده کند -->>
        populateChatList();
    }

    private void openGroupChat(Group group) {
        try {
            if (this.client != null) this.client.removeMessageListener(this);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatPage.fxml"));
            Parent root = loader.load();

            ChatController controller = loader.getController();
            controller.initGroupChat(group); // Initialize the unified controller

            Stage stage = (Stage) chatsList.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openChanelChat(Chanel chanel) {
        try {
            if (this.client != null) {
                this.client.removeMessageListener(this);
            }

            // *** از ChatPage.fxml استفاده می‌کنیم ***
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatPage.fxml"));
            Parent root = loader.load();

            // *** کنترلر یکپارچه را می‌گیریم ***
            ChatController controller = loader.getController();
            // *** و آن را برای حالت کانال مقداردهی اولیه می‌کنیم ***
            controller.initChannelChat(chanel);

            Stage stage = (Stage) chatsList.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    private void addChanelItem(Chanel chanel) {
        HBox chanelItem = new HBox();
        chanelItem.setAlignment(Pos.CENTER_LEFT);
        chanelItem.setStyle("-fx-padding: 15; -fx-cursor: hand;");
        chanelItem.setOnMouseEntered(e -> chanelItem.setStyle("-fx-background-color: #3d4354; -fx-padding: 15; -fx-cursor: hand;"));
        chanelItem.setOnMouseExited(e -> chanelItem.setStyle("-fx-background-color: transparent; -fx-padding: 15; -fx-cursor: hand;"));

        chanelItem.getProperties().put("chanelId", chanel.getChanelID());
        chanelItem.getProperties().put("isChanel", true);

        chanelItem.setOnMouseClicked(e -> {
            openChanelChat(chanel);
        });

        // 3. ساخت آواتار و برچسب‌ها
        String avatarText = chanel.getChanelName().substring(0, 1).toUpperCase();
        Label avatarLabel = new Label(avatarText);
        avatarLabel.setFont(Font.font("Arial Bold", 16));
        avatarLabel.setTextFill(Color.WHITE);
        avatarLabel.setStyle("-fx-background-color: #8a2be2; -fx-background-radius: 22.5; -fx-min-width: 45; -fx-min-height: 45; -fx-alignment: center;");

        VBox textBox = new VBox();
        textBox.setSpacing(3.0);
        textBox.setPadding(new Insets(0, 0, 0, 12));
        textBox.setPrefWidth(180.0);

        Label nameLabel = new Label(chanel.getChanelName());
        nameLabel.setFont(Font.font("Arial", 14));
        nameLabel.setTextFill(Color.WHITE);

        Label typeLabel = new Label("Channel");
        typeLabel.setFont(Font.font("Arial", 12));
        typeLabel.setTextFill(Color.LIGHTGRAY);

        textBox.getChildren().addAll(nameLabel, typeLabel);

        VBox rightBox = new VBox();
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        rightBox.setSpacing(8.0);

        // 4. اضافه کردن نشانگر پیام جدید
        Circle newMessageIndicator = new Circle(5, Color.LIMEGREEN);
        newMessageIndicator.setVisible(false);
        rightBox.getChildren().add(newMessageIndicator);

        chanelItem.getProperties().put("newMessageIndicator", newMessageIndicator);
        chanelItem.getChildren().addAll(avatarLabel, textBox, rightBox);

        chatsList.getChildren().add(chanelItem);
    }

    private void populateChatList() {
        chatsList.getChildren().clear();

        if (UserData.currentUser == null) {
            Label loginLabel = new Label("Please log in.");
            loginLabel.setTextFill(Color.GRAY);
            chatsList.getChildren().add(loginLabel);
            return;
        }

        // 1. چت‌های خصوصی را بارگذاری و اضافه می‌کنیم
        List<User> chatList = DatabaseHelper.getChatListUsers(UserData.currentUser.getUserId());
        if (!chatList.isEmpty()) {
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

        // 2. گروه‌ها را بارگذاری و اضافه می‌کنیم
        List<Group> userGroups = DatabaseHelper.getGroupsForUser(UserData.currentUser.getUserId());
        if (userGroups != null && !userGroups.isEmpty()) {
            for (Group group : userGroups) {
                addGroupItem(group);
            }
        }

        // 3. کانال‌ها را بارگذاری و اضافه می‌کنیم
        List<Chanel> userChannels = DatabaseHelper.getChannelsForUser(UserData.currentUser.getUserId());
        if (userChannels != null && !userChannels.isEmpty()) {
            for (Chanel chanel : userChannels) {
                addChanelItem(chanel);
            }
        }

        // 4. اگر هیچ چت، گروه یا کانالی وجود نداشت، یک پیام نمایش می‌دهیم
        if (chatsList.getChildren().isEmpty()) {
            Label noChatsLabel = new Label("No chats, groups or channels available.");
            noChatsLabel.setTextFill(Color.GRAY);
            noChatsLabel.setFont(Font.font("Arial", 14));
            chatsList.getChildren().add(noChatsLabel);
        }
    }

    private Stage stage;
    private Scene scene;
    @FXML
    private void SwitchToCreateGroupPage(MouseEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("CreatNewGroup.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML private void handleLogout(MouseEvent event) throws Exception {
        try {
            if (this.client != null) {
                this.client.removeMessageListener(this);
                this.client.closeEverything();
                System.out.println("Client disconnected successfully.");
            }
            UserData.currentUser = null;
            UserData.firstName = null;
            UserData.lastName = null;
            UserData.avatarPath = null;
            UserData.PhoneNumber = null;
            switchToLogin();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
        }
    }

    // In class: HomePageController.java

//    @FXML
//    private void handleLogout(MouseEvent event) {
//        // مرحله ۱: ایجاد و نمایش پنجره تایید
//        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
//        alert.setTitle("Log Out");
//        alert.setHeaderText("You are about to log out.");
//        alert.setContentText("Are you sure?");
//
//        Optional<ButtonType> result = alert.showAndWait();
//
//        // اگر کاربر روی دکمه OK کلیک کرد
//        if (result.isPresent() && result.get() == ButtonType.OK) {
//            try {
//                System.out.println("Logging out...");
//
//                // مرحله ۲: قطع اتصال از سرور و پاک‌سازی اطلاعات کاربر
//                ClientManager.disconnect();
//                UserData.currentUser = null;
//
//                // مرحله ۳: بازگشت به صفحه لاگین
//                // از متد استاتیکی که قبلاً در SceneController ساخته‌ایم استفاده می‌کنیم
//                SceneController.switchToLogin();
//
//            } catch (IOException e) {
//                e.printStackTrace();
//                // در صورت بروز خطا، یک پیام مناسب نمایش بده
//                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
//                errorAlert.setTitle("Error");
//                errorAlert.setHeaderText("Logout Failed");
//                errorAlert.setContentText("An error occurred while trying to log out.");
//                errorAlert.showAndWait();
//            }
//        }
//    }
    @FXML private void unhighlightMenuItem(MouseEvent event) {}
    @FXML private void highlightMenuItem(MouseEvent event) {}

}
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
import javafx.scene.image.Image;
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
import org.example.model.User;  // تغییر به model

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;
import org.example.database.DatabaseHelper;
import org.example.projectbackend.Channel;
import org.example.projectbackend.Group;
import org.example.model.User;
import java.util.UUID;

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
    @FXML private boolean menuVisible = false;
    @FXML private javafx.scene.control.TextField searchField;

    public HomePageController(VBox chatsList) {
        this.chatsList = chatsList;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (UserData.currentUser != null) {
            User user = UserData.currentUser;

            // نام
            String fullName = user.getFirstName() + (user.getLastName() != null ? " " + user.getLastName() : "");
            setAccountName(fullName);

            // شماره
            if (menuPhoneNumber != null) {
                menuPhoneNumber.setText(user.getPhoneNumber());
            }

            // آواتار
            if (user.getAvatarPath() != null && !user.getAvatarPath().isEmpty()) {
                setAvatar(user.getAvatarPath());
            }
        } else {
            setAccountName("user");
        }

        setupAvatar();
        initializeMenu();
        rebuildChatsList();
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

    private void setupSampleChats() {
        if (chatsList != null) {
            chatsList.getChildren().clear();
            addChatItem("Telegram", "Welcome to Telegram!", "10:30 AM", "");
            addChatItem("Ali Mohammadi", "Hello there!", "9:15 AM", "3");
            addChatItem("Sara Johnson", "How are you?", "Yesterday", "1");
            addChatItem("Work Group", "Meeting at 3 PM", "12/12/2023", "12");
            addChatItem("John Smith", "See you tomorrow", "Monday", "");
        }
    }

    private void addChatItem(String name, String lastMessage, String time, String unreadCount) {
        HBox chatItem = new HBox();
        chatItem.setAlignment(Pos.CENTER_LEFT);
        chatItem.setStyle("-fx-padding: 15; -fx-cursor: hand;");
        chatItem.setOnMouseEntered(e -> chatItem.setStyle("-fx-background-color: #3d4354; -fx-padding: 15; -fx-cursor: hand;"));
        chatItem.setOnMouseExited(e -> chatItem.setStyle("-fx-background-color: transparent; -fx-padding: 15; -fx-cursor: hand;"));

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
        // TODO: Implement logout functionality
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

    @FXML private Label menuPhoneNumber;

    public void setPhoneNumber(String phone) {
        menuPhoneNumber.setText(phone);
    }

    public void setAccountName(String name) {
        if (userNameLabel != null) userNameLabel.setText(name);
        if (menuAccountName != null) menuAccountName.setText(name);
    }


    public void setFirstName(String firstName) {
        UserData.firstName = firstName;
        setAccountName(firstName);
    }

    @FXML
    private void handleChatItemClick(MouseEvent event) {
        try {
            String partnerName = getPartnerNameFromChatList((Node) event.getSource());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatPage.fxml"));
            Parent root = loader.load();

            ChatController controller = loader.getController();
            controller.setPartnerName(partnerName); // نام رو صحیح تنظیم کن

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSearch() {
        String q = (searchField.getText() == null ? "" : searchField.getText()).trim().toLowerCase();

        // فقط آیتم‌های HBox (چت‌ها) را فیلتر کن؛ هدرهای سکشن (Label) همیشه بمانند
        for (Node n : chatsList.getChildren()) {
            if (n instanceof HBox) {
                HBox row = (HBox) n;
                // فرض: اولین فرزند HBox یک Label عنوان است (chatRow همین را می‌سازد)
                String title = "";
                if (!row.getChildren().isEmpty() && row.getChildren().get(0) instanceof Label) {
                    title = ((Label) row.getChildren().get(0)).getText().toLowerCase();
                }
                boolean match = q.isEmpty() || title.contains(q);
                row.setVisible(match);
                row.setManaged(match);
            }
        }
    }

    // متد جدید برای پیدا کردن HBox والد
    private HBox findParentHBox(Node node) {
        Node parent = node;
        int maxDepth = 10; // برای جلوگیری از infinite loop

        while (parent != null && maxDepth > 0) {
            if (parent instanceof HBox) {
                return (HBox) parent;
            }
            parent = parent.getParent();
            maxDepth--;
        }
        return null;
    }

    // متد جدید برای پیدا کردن نام از طریق موقعیت در لیست
    private String getPartnerNameFromChatList(Node clickedNode) {
        try {
            // پیدا کردن VBox والد (لیست چت‌ها)
            Node parent = clickedNode;
            while (parent != null && !(parent instanceof VBox)) {
                parent = parent.getParent();
            }

            if (parent instanceof VBox) {
                VBox chatsList = (VBox) parent;

                // پیدا کردن آیتمی که شامل node کلیک شده هست
                for (Node chatItem : chatsList.getChildren()) {
                    if (chatItem instanceof HBox) {
                        HBox hbox = (HBox) chatItem;
                        // بررسی آیا این HBox شامل node کلیک شده هست
                        if (containsNode(hbox, clickedNode)) {
                            return findPartnerNameInHBox(hbox);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown User";
    }

    private boolean containsNode(Parent parent, Node targetNode) {
        if (parent == targetNode) {
            return true;
        }

        for (Node child : parent.getChildrenUnmodifiable()) {
            if (child == targetNode) {
                return true;
            }
            if (child instanceof Parent) {
                if (containsNode((Parent) child, targetNode)) {
                    return true;
                }
            }
        }
        return false;
    }

    // در HomePageController.java
    private String findPartnerNameInHBox(HBox chatItem) {
        try {
            // پیدا کردن اولین Label که نام کاربر باشد
            for (Node child : chatItem.getChildren()) {
                if (child instanceof Label) {
                    Label label = (Label) child;
                    if (label.getText() != null && !label.getText().contains(":") &&
                            !label.getText().contains("last seen") && label.getText().length() < 50) {
                        return label.getText();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "User";
    }

    private void rebuildChatsList() {
        chatsList.getChildren().clear();

        // ✅ بخش Private Chats
        chatsList.getChildren().add(sectionHeader("Private Chats"));
        var contacts = DatabaseHelper.getContacts(UserData.currentUser.getUserId());
        for (User u : contacts) {
            chatsList.getChildren().add(
                    chatRow(u.getUserName(),
                            () -> openPrivateChat(u.getUserId(), u.getUserName()))
            );
        }

        // ✅ بخش Groups
        chatsList.getChildren().add(sectionHeader("Groups"));
        var groups = DatabaseHelper.getGroupsForUser(UserData.currentUser.getUserId());
        for (var g : groups) {
            chatsList.getChildren().add(
                    chatRow(g.getGroupName(),
                            () -> openGroupChat(g.getGroupId(), g.getGroupName()))
            );
        }

        // ✅ بخش Channels
        chatsList.getChildren().add(sectionHeader("Channels"));
        var myChannels = DatabaseHelper.getOwnedChannels(UserData.currentUser.getUserId());
        var subChannels = DatabaseHelper.getSubscribedChannels(UserData.currentUser.getUserId());

        for (var c : myChannels) {
            chatsList.getChildren().add(
                    chatRow(c.getChannelName() + " (owner)",
                            () -> openChannelChat(c.getChannelId(), c.getChannelName(), true))
            );
        }
        for (var c : subChannels) {
            if (!c.getOwnerId().equals(UserData.currentUser.getUserId())) {
                chatsList.getChildren().add(
                        chatRow(c.getChannelName(),
                                () -> openChannelChat(c.getChannelId(), c.getChannelName(), false))
                );
            }
        }
    }


    private void openPrivateChat(UUID userId, String userName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatPage.fxml"));
            Parent root = loader.load();

            ChatController cc = loader.getController();
            cc.openAsPrivate(userId, userName); // 🚀 انتقال کاربر و نام

            Stage stage = (Stage) chatsList.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 600));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private Label sectionHeader(String title) {
        Label l = new Label(title);
        l.setStyle("-fx-text-fill: #888; -fx-padding: 8 12 4 12; -fx-font-size: 12;");
        return l;
    }

    private HBox chatRow(String title, Runnable onClick) {
        Label name = new Label(title);
        name.setStyle("-fx-text-fill: #222; -fx-font-size: 14; -fx-padding: 12;");

        HBox row = new HBox(name);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-cursor: hand;");
        row.setOnMouseClicked(e -> onClick.run());
        return row;
    }


    private void openGroupChat(UUID groupId, String groupName) {
        try {
            FXMLLoader loader = new FXMLLoader(AppSceneController.class.getResource("ChatPage.fxml"));
            Parent root = loader.load();
            ChatController cc = loader.getController();
            cc.openAsGroup(groupId, groupName); // مرحله ۴
            Stage st = (Stage) chatsList.getScene().getWindow();
            st.setScene(new Scene(root, 1000, 600));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openChannelChat(UUID channelId, String channelName, boolean isOwner) {
        try {
            FXMLLoader loader = new FXMLLoader(AppSceneController.class.getResource("ChatPage.fxml"));
            Parent root = loader.load();
            ChatController cc = loader.getController();
            cc.openAsChannel(channelId, channelName, isOwner); // مرحله ۴
            Stage st = (Stage) chatsList.getScene().getWindow();
            st.setScene(new Scene(root, 1000, 600));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ImageView getMenuLines() {
        return MenuLines;
    }

    public void setMenuLines(ImageView menuLines) {
        MenuLines = menuLines;
    }

    public void setAvatar(String path) {
        Image image;
        if (path != null && !path.isEmpty()) {
            image = new Image(new File(path).toURI().toString());
        } else {
            image = new Image(getClass().getResource("/org/example/images/default_avatar.png").toExternalForm());
        }

        if (userAvatar != null) {
            userAvatar.setImage(image);
            Circle clip = new Circle(22.5, 22.5, 22.5);
            userAvatar.setClip(clip);
        }
        if (menuAvatar != null) {
            menuAvatar.setImage(image);
            Circle menuClip = new Circle(35, 35, 35);
            menuAvatar.setClip(menuClip);
        }
    }



    public HomePageController() {
        // لازم نیست چیزی بنویسی
    }


}
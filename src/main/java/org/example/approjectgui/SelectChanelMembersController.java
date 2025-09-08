package org.example.approjectgui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.database.DatabaseHelper;
import org.example.model.User;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class SelectChanelMembersController implements Initializable {

    @FXML private VBox contactsListContainer;
    @FXML private ScrollPane contactsScrollPane;
    private Scene scene;

    private List<UUID> selectedMembers = new ArrayList<>();
    private List<User> selectedUserObjects = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadContacts();
    }

    public void loadContacts() {
        contactsListContainer.getChildren().clear();

        if (UserData.currentUser != null) {
            List<User> contactUsers = DatabaseHelper.getContactsAsUsers(UserData.currentUser.getUserId());

            if (contactUsers.isEmpty()) {
                Label noContactsLabel = new Label("No contacts yet. Add some!");
                noContactsLabel.setTextFill(Color.GRAY);
                contactsListContainer.getChildren().add(noContactsLabel);
            } else {
                for (User contactUser : contactUsers) {
                    addContactItem(contactUser);
                }
            }
        } else {
            Label loginLabel = new Label("Please log in to see your contacts");
            contactsListContainer.getChildren().add(loginLabel);
        }
    }

    private void addContactItem(User contactUser) {
        HBox contactItem = new HBox();
        contactItem.setAlignment(Pos.CENTER_LEFT);
        contactItem.setStyle("-fx-padding: 15; -fx-cursor: hand;");
        contactItem.setOnMouseEntered(e -> contactItem.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 15; -fx-cursor: hand;"));
        contactItem.setOnMouseExited(e -> contactItem.setStyle("-fx-background-color: transparent; -fx-padding: 15; -fx-cursor: hand;"));

        contactItem.setOnMouseClicked(e -> {
            toggleUserSelection(contactUser, contactItem);
        });

        String firstName = contactUser.getFirstName() != null ? contactUser.getFirstName() : "?";
        String avatarText = firstName.substring(0, 1).toUpperCase();
        Label avatarLabel = new Label(avatarText);
        avatarLabel.setFont(Font.font("Arial Bold", 16));
        avatarLabel.setTextFill(Color.WHITE);
        avatarLabel.setStyle("-fx-background-color: #0088cc; -fx-background-radius: 20; -fx-min-width: 40; -fx-min-height: 40; -fx-alignment: center;");

        VBox textBox = new VBox(5);
        textBox.setPadding(new Insets(0, 0, 0, 15));

        Label nameLabel = new Label(contactUser.getUserName());
        nameLabel.setFont(Font.font("Arial", 14));
        Label phoneLabel = new Label(contactUser.getPhoneNumber());
        phoneLabel.setFont(Font.font("Arial", 12));
        phoneLabel.setTextFill(Color.GRAY);

        textBox.getChildren().addAll(nameLabel, phoneLabel);
        contactItem.getChildren().addAll(avatarLabel, textBox);
        contactsListContainer.getChildren().add(contactItem);
    }

    private void toggleUserSelection(User user, HBox contactItem) {
        UUID userId = user.getUserId();
        if (selectedMembers.contains(userId)) {
            selectedMembers.remove(userId);
            selectedUserObjects.remove(user);
            contactItem.setStyle("-fx-background-color: transparent; -fx-padding: 15; -fx-cursor: hand;");
            System.out.println("User deselected: " + user.getUserName());
        } else {
            selectedMembers.add(userId);
            selectedUserObjects.add(user);
            contactItem.setStyle("-fx-background-color: #ddeeff; -fx-padding: 15; -fx-cursor: hand;");
            System.out.println("User selected: " + user.getUserName() + " - UUID: " + userId);
        }
        System.out.println("Current selected members: " + selectedMembers);
    }

    @FXML
    private void BackArrow(MouseEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("CreatNewChanel.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void createChanelWithSelectedMembers(MouseEvent event) throws IOException {
        if (selectedMembers.isEmpty()) {
            System.out.println("Please select at least one member.");
            return;
        }

        // اضافه کردن کاربر جاری به عنوان مالک کانال
        selectedMembers.add(UserData.currentUser.getUserId());

        // 1. ذخیره کانال در دیتابیس
        boolean success = DatabaseHelper.createChanel(
                ChanelData.chanelName,
                UserData.currentUser.getUserId(),
                selectedMembers
        );

        if (success) {

            // 2. پاک کردن داده‌های موقت
            ChanelData.clearData();

            // 3. هدایت به صفحه اصلی
            Parent root = FXMLLoader.load(getClass().getResource("HomePage.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } else {
            System.out.println("Failed to create channel. Please try again.");
            // می‌تونی یک Alert به کاربر نشان بدی
        }
    }
}
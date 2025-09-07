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
import java.util.List;
import java.util.ResourceBundle;

public class ContactController implements Initializable {

    @FXML
    private VBox contactsList;
    @FXML
    private ScrollPane contactsScrollPane;

    private Stage stage;
    private Scene scene;
    private Parent root;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadContacts(); // <<-- نام متد برای وضوح بیشتر تغییر کرد
    }

    // <<-- این متد بازنویسی شده تا مستقیماً با لیست کاربران کار کند
    public void loadContacts() {
        contactsList.getChildren().clear();

        if (UserData.currentUser != null) {
            // <<-- استفاده از متد قابل اعتماد getContactsAsUsers
            List<User> contactUsers = DatabaseHelper.getContactsAsUsers(UserData.currentUser.getUserId());

            if (contactUsers.isEmpty()) {
                Label noContactsLabel = new Label("No contacts yet. Add some!");
                noContactsLabel.setTextFill(Color.GRAY);
                contactsList.getChildren().add(noContactsLabel);
            } else {
                for (User contactUser : contactUsers) {
                    addContactItem(contactUser);
                }
            }
        } else {
            Label loginLabel = new Label("Please log in to see your contacts");
            contactsList.getChildren().add(loginLabel);
        }
    }

    // <<-- این متد بازنویسی شده تا به جای Contact، یک User دریافت کند
    private void addContactItem(User contactUser) {
        HBox contactItem = new HBox();
        contactItem.setAlignment(Pos.CENTER_LEFT);
        contactItem.setStyle("-fx-padding: 15; -fx-cursor: hand;");
        contactItem.setOnMouseEntered(e -> contactItem.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 15; -fx-cursor: hand;"));
        contactItem.setOnMouseExited(e -> contactItem.setStyle("-fx-background-color: transparent; -fx-padding: 15; -fx-cursor: hand;"));

        // <<-- با کلیک، خود آبجکت User به متد بعدی پاس داده می‌شود
        contactItem.setOnMouseClicked(e -> {
            try {
                openChatWithUser(contactUser);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
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
        contactsList.getChildren().add(contactItem);
    }

    // <<-- این متد بازنویسی شده تا User دریافت کند و بلوک else خطرناک را حذف کند
    private void openChatWithUser(User partnerUser) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatPage.fxml"));
        Parent root = loader.load();
        ChatController chatController = loader.getController();

        // <<-- همیشه از UUID و نام کاربر واقعی استفاده می‌شود. دیگر UUID تصادفی وجود ندارد.
        chatController.setPartner(partnerUser.getUserName(), partnerUser.getUserId());

        Stage currentStage = (Stage) contactsList.getScene().getWindow();
        currentStage.setScene(new Scene(root));
        currentStage.show();
    }

    @FXML
    private void SwitchToNewContactPage(MouseEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("NewContactPage.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void BackArrow(MouseEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("HomePage.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
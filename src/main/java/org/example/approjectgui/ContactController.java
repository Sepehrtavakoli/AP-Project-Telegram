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
import org.example.projectbackend.Contact;

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
        loadContactsFromDatabase();
    }

    public void loadContactsFromDatabase() {
        // پاک کردن لیست موجود
        contactsList.getChildren().clear();

        // گرفتن مخاطبان از دیتابیس
        if (UserData.currentUser != null) {
            List<Contact> contacts = DatabaseHelper.getContactsByUserId(UserData.currentUser.getUserId());

            if (contacts.isEmpty()) {
                // اگر مخاطبی وجود ندارد، پیام مناسب نمایش دهید
                Label noContactsLabel = new Label("No contacts yet. Add some contacts to see them here.");
                noContactsLabel.setFont(Font.font("Arial", 14));
                noContactsLabel.setTextFill(Color.GRAY);
                noContactsLabel.setPadding(new Insets(20));
                noContactsLabel.setAlignment(Pos.CENTER);
                contactsList.getChildren().add(noContactsLabel);
            } else {
                // اضافه کردن مخاطبان به لیست
                for (Contact contact : contacts) {
                    addContactItem(contact);
                }
            }
        } else {
            Label loginLabel = new Label("Please log in to see your contacts");
            loginLabel.setFont(Font.font("Arial", 14));
            loginLabel.setTextFill(Color.GRAY);
            loginLabel.setPadding(new Insets(20));
            loginLabel.setAlignment(Pos.CENTER);
            contactsList.getChildren().add(loginLabel);
        }
    }

    private void addContactItem(Contact contact) {
        HBox contactItem = new HBox();
        contactItem.setAlignment(Pos.CENTER_LEFT);
        contactItem.setStyle("-fx-padding: 15; -fx-cursor: hand;");
        contactItem.setOnMouseEntered(e -> contactItem.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 15; -fx-cursor: hand;"));
        contactItem.setOnMouseExited(e -> contactItem.setStyle("-fx-background-color: transparent; -fx-padding: 15; -fx-cursor: hand;"));

        // برای کلیک کردن و شروع چت
        contactItem.setOnMouseClicked(e -> {
            try {
                openChatWithContact(contact);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        // Avatar (حرف اول نام)
        String firstName = contact.getFirstName() != null ? contact.getFirstName() : "";
        String avatarText = firstName.isEmpty() ? "?" : firstName.substring(0, 1).toUpperCase();

        Label avatarLabel = new Label(avatarText);
        avatarLabel.setFont(Font.font("Arial Bold", 16));
        avatarLabel.setTextFill(Color.WHITE);
        avatarLabel.setStyle("-fx-background-color: #0088cc; -fx-background-radius: 20; -fx-min-width: 40; -fx-min-height: 40; -fx-alignment: center;");

        // Text content
        VBox textBox = new VBox();
        textBox.setSpacing(2.0);
        textBox.setPadding(new Insets(0, 0, 0, 15));
        textBox.setPrefWidth(250.0);

        String fullName = contact.getFirstName() + (contact.getLastName() != null ? " " + contact.getLastName() : "");
        Label nameLabel = new Label(fullName);
        nameLabel.setFont(Font.font("Arial", 14));
        nameLabel.setTextFill(Color.BLACK);

        Label phoneLabel = new Label(contact.getPhoneNumber());
        phoneLabel.setFont(Font.font("Arial", 12));
        phoneLabel.setTextFill(Color.GRAY);

        textBox.getChildren().addAll(nameLabel, phoneLabel);
        contactItem.getChildren().addAll(avatarLabel, textBox);

        contactsList.getChildren().add(contactItem);
    }

    private void openChatWithContact(Contact contact) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatPage.fxml"));
        Parent root = loader.load();

        ChatController chatController = loader.getController();

        // تنظیم اطلاعات مخاطب برای چت
        if (contact.getContactUser() != null) {
            chatController.setPartner(contact.getContactUser().getUserName(),
                    contact.getContactUser().getUserId());
        } else {
            // اگر contactUser null است، یک کاربر موقت ایجاد کنید
            User tempUser = new User(contact.getFirstName(), null, contact.getPhoneNumber());
            tempUser.setLastName(contact.getLastName());
            chatController.setPartner(contact.getFirstName(), tempUser.getUserId());
        }

        Stage stage = (Stage) contactsList.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
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

    // متد برای رفرش کردن لیست مخاطبان
    public void refreshContactsList() {
        loadContactsFromDatabase();
    }
}
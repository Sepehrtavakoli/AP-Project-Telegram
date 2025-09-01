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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.text.Font;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ContactController implements Initializable {

    @FXML
    private VBox contactsList;

    private Stage stage;
    private Scene scene;
    private Parent root;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupSampleContacts();
    }

    private void setupSampleContacts() {
        // پاک کردن لیست موجود
        contactsList.getChildren().clear();

        // اضافه کردن مخاطبین نمونه
        addContactItem("Ali Mohammadi", "+98 912 345 6789", "online");
        addContactItem("Sara Johnson", "+98 933 123 4567", "last seen recently");
        addContactItem("Mohammad Reza", "+98 921 987 6543", "online");
        addContactItem("Fatemeh Karimi", "+98 935 555 1234", "last seen 2 hours ago");
        addContactItem("John Smith", "+1 234 567 8900", "online");
        addContactItem("Telegram Support", "Telegram", "online");
    }

    private void addContactItem(String name, String phone, String status) {
        HBox contactItem = new HBox();
        contactItem.setAlignment(Pos.CENTER_LEFT);
        contactItem.setStyle("-fx-padding: 15; -fx-cursor: hand;");
        contactItem.setOnMouseEntered(e -> contactItem.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 15; -fx-cursor: hand;"));
        contactItem.setOnMouseExited(e -> contactItem.setStyle("-fx-background-color: transparent; -fx-padding: 15; -fx-cursor: hand;"));

        // Avatar (حرف اول نام)
        Label avatarLabel = new Label(name.substring(0, 1).toUpperCase());
        avatarLabel.setFont(Font.font("Arial Bold", 16));
        avatarLabel.setTextFill(Color.WHITE);
        avatarLabel.setStyle("-fx-background-color: #0088cc; -fx-background-radius: 20; -fx-min-width: 40; -fx-min-height: 40; -fx-alignment: center;");

        // Text content
        VBox textBox = new VBox();
        textBox.setSpacing(2.0);
        textBox.setPadding(new Insets(0, 0, 0, 15));
        textBox.setPrefWidth(250.0);

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Arial", 14));
        nameLabel.setTextFill(Color.BLACK);

        Label statusLabel = new Label(status);
        statusLabel.setFont(Font.font("Arial", 12));
        statusLabel.setTextFill(Color.GRAY);

        textBox.getChildren().addAll(nameLabel, statusLabel);
        contactItem.getChildren().addAll(avatarLabel, textBox);

        contactsList.getChildren().add(contactItem);
    }

    // متدهای navigation که در FXML فراخوانی می‌شوند
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
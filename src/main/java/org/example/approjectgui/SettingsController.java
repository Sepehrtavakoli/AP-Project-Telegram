package org.example.approjectgui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML
    private ImageView avatarImageView;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label phoneNumberLabel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadUserData();
    }

    // In SettingsController.java
    @FXML
    private void switchToMyAccount() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("MyAccountPage.fxml"));
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void loadUserData() {
        if (UserData.currentUser != null) {
            // Set user name and phone number
            userNameLabel.setText(UserData.currentUser.getUserName());
            phoneNumberLabel.setText(UserData.currentUser.getPhoneNumber());

            // Set user avatar
            String avatarPath = UserData.currentUser.getAvatarPath();
            if (avatarPath != null && !avatarPath.isEmpty()) {
                File avatarFile = new File(avatarPath);
                if (avatarFile.exists()) {
                    Image avatarImage = new Image(avatarFile.toURI().toString());
                    avatarImageView.setImage(avatarImage);
                }
            } else {
                // Set a default avatar if none is available
                Image defaultAvatar = new Image(getClass().getResourceAsStream("/images/default_avatar.png"));
                avatarImageView.setImage(defaultAvatar);
            }
        }
    }

    @FXML
    private void handleBack() throws IOException {
        // Navigate back to the HomePage
        Parent root = FXMLLoader.load(getClass().getResource("HomePage.fxml"));
        Stage stage = (Stage) userNameLabel.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
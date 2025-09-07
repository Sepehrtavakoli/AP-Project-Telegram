package org.example.approjectgui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.database.DatabaseHelper;
import org.example.model.User;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MyAccountController implements Initializable {

    @FXML
    private ImageView avatarImageView;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private Label phoneNumberLabel;
    @FXML
    private Label statusLabel;

    private String newAvatarPath = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadUserProfile();
    }

    private void loadUserProfile() {
        User currentUser = UserData.currentUser;
        if (currentUser != null) {
            firstNameField.setText(currentUser.getFirstName());
            lastNameField.setText(currentUser.getLastName());
            phoneNumberLabel.setText(currentUser.getPhoneNumber());

            newAvatarPath = currentUser.getAvatarPath(); // Keep track of the current path
            if (newAvatarPath != null && !newAvatarPath.isEmpty()) {
                File file = new File(newAvatarPath);
                if (file.exists()) {
                    avatarImageView.setImage(new Image(file.toURI().toString()));
                }
            } else {
                avatarImageView.setImage(new Image(getClass().getResourceAsStream("/images/default_avatar.png")));
            }
        }
    }

    @FXML
    private void changeAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose New Avatar");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(avatarImageView.getScene().getWindow());
        if (file != null) {
            newAvatarPath = file.getAbsolutePath();
            avatarImageView.setImage(new Image(file.toURI().toString()));
        }
    }

    // متد handleSave را به طور کامل جایگزین کنید
    @FXML
    private void handleSave() {
        User currentUser = UserData.currentUser;
        if (currentUser == null) return;

        // آپدیت کردن آبجکت در حافظه برنامه
        currentUser.setFirstName(firstNameField.getText().trim());
        currentUser.setLastName(lastNameField.getText().trim());
        currentUser.setAvatarPath(newAvatarPath);

        // ذخیره دائمی تغییرات در دیتابیس با استفاده از متد جدید
        boolean success = DatabaseHelper.updateUser(currentUser);

        if (success) {
            System.out.println("Profile updated successfully in DB.");
            // بعد از ذخیره موفق، به صفحه تنظیمات برگرد
            try {
                handleBack();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            statusLabel.setText("Failed to update profile.");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    // متد handleBack را هم جایگزین کنید تا صفحه تنظیمات را رفرش کند
    @FXML
    private void handleBack() throws IOException {
        // با بارگذاری مجدد، کنترلر صفحه تنظیمات مجبور به خواندن اطلاعات جدید می‌شود
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SettingsPage.fxml"));
        Parent root = loader.load();

        // این کار تضمین می‌کند که متد initialize در SettingsController دوباره فراخوانی شود
        // و اطلاعات جدید نمایش داده شود.

        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
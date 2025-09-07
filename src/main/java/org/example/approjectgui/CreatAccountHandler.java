package org.example.approjectgui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.database.DatabaseHelper;
import org.example.model.User;  // تغییر به model

import java.io.File;
import java.io.IOException;

public class CreatAccountHandler {

    @FXML private TextField FirstName;
    @FXML public TextField LastName;
    @FXML private ImageView Avatar;

    private String avatarPath;

    public void SetAvatar(javafx.scene.input.MouseEvent mouseEvent) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Avatar");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File SelectedFile = fileChooser.showOpenDialog(Avatar.getScene().getWindow());

        if (SelectedFile != null) {
            // ذخیره مسیر فایل
            avatarPath = SelectedFile.getAbsolutePath();

            Image image = new Image(SelectedFile.toURI().toString());
            Avatar.setImage(image);

            Avatar.setFitHeight(150);
            Avatar.setFitWidth(150);
            Avatar.setPreserveRatio(true);

            Circle circle = new Circle(75,75,75);
            Avatar.setClip(circle);
        }
    }

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private void SwitchToHomePage(ActionEvent event) {
        try {
            String firstName = FirstName.getText();
            String lastName = LastName.getText();

            if (firstName == null || firstName.isEmpty()) {
                System.out.println("⚠ Please enter your first name.");
                return;
            }

            // Save into UserData
            UserData.firstName = firstName;
            UserData.lastName = lastName;
            UserData.avatarPath = avatarPath;

            // Create user and save to DB
            User user = new User(firstName, avatarPath, UserData.PhoneNumber);
            user.setLastName(lastName);

            boolean saved = DatabaseHelper.addUser(user);
            if (saved) {
                System.out.println("✓ User saved to database: " + user.getUserId());
            } else {
                System.out.println("✗ Failed to save user to database");
            }

            UserData.currentUser = user;

            // Load HomePage
            FXMLLoader loader = new FXMLLoader(getClass().getResource("HomePage.fxml"));
            Parent root = loader.load();

            // Pass name + avatar to HomePage
            HomePageController homeController = loader.getController();
            homeController.setAccountName(UserData.firstName);
            if (avatarPath != null && !avatarPath.isEmpty()) {
                homeController.setAvatar(avatarPath);
            }

            homeController.setPhoneNumber(UserData.PhoneNumber);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void SwitchToLogin(javafx.scene.input.MouseEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 360, 500);
        stage.setScene(scene);
        stage.show();
    }


}
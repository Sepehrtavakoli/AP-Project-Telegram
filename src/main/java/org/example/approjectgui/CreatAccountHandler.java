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
import org.example.projectbackend.User;

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
            UserData.firstName = FirstName.getText();
            UserData.lastName = LastName.getText();
            UserData.avatarPath = avatarPath;

            User user = new User(UserData.firstName, UserData.avatarPath, UserData.PhoneNumber);

            UserData.currentUser = user;

            Parent root = FXMLLoader.load(getClass().getResource("HomePage.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void SwitchToLogin(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
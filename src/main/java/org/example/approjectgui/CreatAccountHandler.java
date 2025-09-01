package org.example.approjectgui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class CreatAccountHandler {

    @FXML
    private TextField FirstName;

    @FXML
    private TextField LastName;

    @FXML
    private ImageView Avatar;

    @FXML
    private Button Continue;

    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        setupValidation();
    }

    private void setupValidation() {
        FirstName.textProperty().addListener((observable, oldValue, newValue) -> {
            validateForm();
        });

        LastName.textProperty().addListener((observable, oldValue, newValue) -> {
            validateForm();
        });
    }

    private void validateForm() {
        boolean isValid = !FirstName.getText().trim().isEmpty();
        Continue.setDisable(!isValid);
    }

    public void SetAvatar(MouseEvent mouseEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Photo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(Avatar.getScene().getWindow());

        if (selectedFile != null) {
            try {
                Image image = new Image(selectedFile.toURI().toString());
                Avatar.setImage(image);

                // Make avatar circular
                Circle clip = new Circle(48, 48, 48);
                Avatar.setClip(clip);

                Avatar.setFitHeight(96);
                Avatar.setFitWidth(96);
                Avatar.setPreserveRatio(true);

            } catch (Exception e) {
                showError("Failed to load image");
            }
        }
    }

    public void SwitchToHomePage(ActionEvent event) {
        String firstName = FirstName.getText().trim();
        String lastName = LastName.getText().trim();

        if (firstName.isEmpty()) {
            showError("Please enter your first name");
            return;
        }

        try {
            // Save user profile data here
            System.out.println("Creating account for: " + firstName + " " + lastName);

            // Navigate to home page
            SceneController.switchToHomePage();
        } catch (Exception e) {
            showError("Error creating account: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void SwitchToLogin(ActionEvent event) {
        try {
            SceneController.switchToLogin();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        // You can add error label to FXML and show errors here
        System.out.println("Error: " + message);
    }
}
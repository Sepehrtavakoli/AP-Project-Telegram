package org.example.approjectgui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneController {
    private Scene scene;
    private Stage stage;
    private Parent root;

    // Existing methods...
    public void SwitchToLogin(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root, 360, 500);
        stage.setScene(scene);
        stage.show();
    }


    public static void switchToCreatAccountPage() throws IOException {
        Stage stage = (Stage) javafx.stage.Window.getWindows().get(0);
        Parent root = FXMLLoader.load(SceneController.class.getResource("CreatAccount.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void SwitchToHomePage(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("HomePage.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private PasswordField PassCodeField;

    LoginController loginController = new LoginController();

    @FXML
    Label CheckAlert;

    public void CheckPassCode(ActionEvent event) throws IOException {
        String passCode = PassCodeField.getText();
        if (passCode.matches("\\d{4}") && Integer.parseInt(passCode) == loginController.PassCode) {
            CheckAlert.setText("Login Successful");
            SwitchToCreatAccountPage(event);
        } else {
            CheckAlert.setText("Wrong Password\nPlease try again");
        }
    }

    public void SwitchToCreatAccountPage(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("CreatAccount.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void SwitchToLogin2(MouseEvent mouseEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        stage = (Stage)((Node)mouseEvent.getSource()).getScene().getWindow();
        scene = new Scene(root, 360, 500);
        stage.setScene(scene);
        stage.show();
    }

    // NEW: Method to switch to verification page
    public static void switchToVerification() throws IOException {
        Stage stage = (Stage) javafx.stage.Window.getWindows().get(0);
        Parent root = FXMLLoader.load(SceneController.class.getResource("VerificationPage.fxml"));
        Scene scene = new Scene(root, 360, 500);
        stage.setScene(scene);
        stage.show();
    }

    public static void switchToHomePage() throws IOException {
        Stage stage = (Stage) javafx.stage.Window.getWindows().get(0);
        Parent root = FXMLLoader.load(SceneController.class.getResource("HomePage.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    // NEW: Method to switch to login page
    public static void switchToLogin() throws IOException {
        Stage stage = (Stage) javafx.stage.Window.getWindows().get(0);
        Parent root = FXMLLoader.load(SceneController.class.getResource("login.fxml"));
        Scene scene = new Scene(root, 360, 500);
        stage.setScene(scene);
        stage.show();
    }

    // این متد رو به SceneController اضافه کن:
    public static void switchToContactPage() throws IOException {
        Stage stage = (Stage) javafx.stage.Window.getWindows().get(0);
        Parent root = FXMLLoader.load(SceneController.class.getResource("ContactPage.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
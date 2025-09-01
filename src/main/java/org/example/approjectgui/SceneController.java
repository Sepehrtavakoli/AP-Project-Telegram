package org.example.approjectgui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class SceneController {
    private Scene scene;
    private Stage stage;
    private Parent root;

    public void SwitchToLogin(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Login.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
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
        if (Integer.parseInt(passCode)==loginController.PassCode) {
            CheckAlert.setText("Login Successful");
            SwitchToCreatAccountPage(event);
        }
        else {
            CheckAlert.setText("Wrong Password\nPlease try again");
        }
    }

    public void SwitchToCreatAccountPage (ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("CreatAccount.fxml"));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }


    public void SwitchToLogin2(javafx.scene.input.MouseEvent mouseEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        stage = (Stage)((Node)mouseEvent.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
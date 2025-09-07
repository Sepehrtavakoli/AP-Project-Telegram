package org.example.approjectgui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppSceneController {

    public static void switchToLogin() throws Exception {
        Stage stage = getCurrentStage();
        Scene scene = new Scene(FXMLLoader.load(AppSceneController.class.getResource("login.fxml")), 360, 500);
        stage.setScene(scene);
    }

    public static void switchToVerification() throws Exception {
        Stage stage = getCurrentStage();
        Scene scene = new Scene(FXMLLoader.load(AppSceneController.class.getResource("VerificationPage.fxml")), 360, 500);
        stage.setScene(scene);
    }

    private static Stage getCurrentStage() {
        return (Stage) javafx.stage.Window.getWindows().get(0);
    }
}
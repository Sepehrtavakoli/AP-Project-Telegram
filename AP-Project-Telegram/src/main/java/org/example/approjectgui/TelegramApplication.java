package org.example.approjectgui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.*;

import java.io.IOException;

public class TelegramApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TelegramApplication.class.getResource("login.fxml"));
        Scene loginScene = new Scene(fxmlLoader.load(), 620, 480);

//        Image logo = new Image("images/Picture1.png");
//        stage.getIcons().add(logo);

        stage.setTitle("Telegram");
        stage.setScene(loginScene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
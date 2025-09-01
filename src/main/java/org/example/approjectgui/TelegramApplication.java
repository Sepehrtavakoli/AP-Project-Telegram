package org.example.approjectgui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class TelegramApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TelegramApplication.class.getResource("login.fxml"));
        Scene loginScene = new Scene(fxmlLoader.load(), 360, 500); // اندازه جدید

        stage.setTitle("Telegram");
        stage.setScene(loginScene);
        stage.setResizable(false); // غیرفعال کردن تغییر اندازه پنجره
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
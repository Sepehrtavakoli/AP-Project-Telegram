package org.example.approjectgui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.database.DatabaseHelper;

import java.io.IOException;

public class TelegramApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        try {
            DatabaseHelper.initializeDatabase();
        } catch (Exception e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }


        FXMLLoader fxmlLoader = new FXMLLoader(TelegramApplication.class.getResource("login.fxml"));
        Scene loginScene = new Scene(fxmlLoader.load(), 360, 500);

        stage.setTitle("Telegram");
        stage.setScene(loginScene);
        stage.setResizable(false);
        stage.show();
    }


    @Override
    public void stop() throws Exception {
        System.out.println("Application is closing. Disconnecting client...");
        org.example.API.ClientManager.disconnect();
        DatabaseHelper.disconnect();
        super.stop();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
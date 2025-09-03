package org.example.approjectgui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class PopupController {

    @FXML
    private Label codeLabel;

    public static void showVerificationCode(int code) {
        try {
            FXMLLoader loader = new FXMLLoader(PopupController.class.getResource("VerificationPopup.fxml"));
            VBox popupContent = loader.load();

            PopupController controller = loader.getController();
            controller.codeLabel.setText("Code: " + code);

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.initStyle(StageStyle.UTILITY);
            popupStage.setTitle("Verification Code");
            popupStage.setScene(new Scene(popupContent));
            popupStage.setResizable(false);
            popupStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            // Fallback: نمایش در ترمینال اگر فایل FXML مشکل داشت
            System.out.println("Verification code: " + code);
        }
    }

    @FXML
    private void closePopup() {
        Stage stage = (Stage) codeLabel.getScene().getWindow();
        stage.close();
    }
}
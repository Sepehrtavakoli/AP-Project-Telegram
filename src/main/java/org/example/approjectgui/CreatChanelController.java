package org.example.approjectgui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class CreatChanelController {

    @FXML private TextField ChanelName;
    @FXML private TextField ChanelDescription;
    @FXML private ImageView ChanelAvatar;


    private Stage stage;
    private Scene scene;

    @FXML
    private void BackArrow(MouseEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("ContactPage.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void CreateChanelWithName(MouseEvent event) throws IOException {
        if (ChanelName.getText().isEmpty()) {
            System.out.println("Please Enter a Chanel name.");
            return;
        }

        ChanelData.chanelName = ChanelName.getText();
        if (ChanelDescription.getText() != null && !ChanelDescription.getText().isEmpty()) {
            ChanelData.chanelDescription = ChanelDescription.getText();
        }

        Parent root = FXMLLoader.load(getClass().getResource("SelectChanelMembers.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}


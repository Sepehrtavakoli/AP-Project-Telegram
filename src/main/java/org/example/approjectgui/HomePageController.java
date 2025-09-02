package org.example.approjectgui;

import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import javafx.scene.input.MouseEvent;

import javafx.scene.control.Label;
import java.io.IOException;

public class HomePageController {

    @FXML
    private ImageView PenImageView;

    Image PenImage = new Image(getClass().getResourceAsStream("/images/Pen.png"));

    private Scene scene;
    private Stage stage;
    private Parent root;


    // در متد SwitchToContactPage تغییر بدیم:
    public void SwitchToContactPage(javafx.scene.input.MouseEvent mouseEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("ContactPage.fxml"));
        stage = (Stage)((Node)mouseEvent.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

    }
    public void SwitchToNewContactPage(javafx.scene.input.MouseEvent mouseEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("NewContactPage.fxml"));
        stage = (Stage)((Node)mouseEvent.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void BackArrow(javafx.scene.input.MouseEvent mouseEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("HomePage.fxml"));
        stage = (Stage)((Node)mouseEvent.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private ImageView MenuLines;

    @FXML
    private AnchorPane sideMenu;

    private boolean menuVisible = false;

    @FXML
    public void initialize() {
        if(sideMenu != null) {
            sideMenu.setTranslateX(-200);
        }
    }

    @FXML
    public Label AccountName;

    public void setAccountName(String name) {
        AccountName.setText(name);
    }

    @FXML
    public void SwitchToMenu(javafx.scene.input.MouseEvent mouseEvent) throws IOException {
        TranslateTransition slide = new TranslateTransition(Duration.millis(300), sideMenu);

        if (menuVisible) {
            slide.setToX(-200);
            menuVisible = false;
        } else {
            slide.setToX(0);
            menuVisible = true;
        }

        slide.play();
    }

    @FXML
    private Label Contacts;
}
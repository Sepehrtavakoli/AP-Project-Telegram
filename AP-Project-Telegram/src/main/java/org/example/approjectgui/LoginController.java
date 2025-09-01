package org.example.approjectgui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

import javafx.stage.Stage;
import org.example.model.country;
import org.example.util.CountryLoader;

public class LoginController implements Initializable {

    public ImageView logoImage;

    //----------------------------//


    @FXML
    private ChoiceBox<country> countryChoicebox;
    @FXML
    private Label Ccode;
    @FXML
    private Label checkLogin;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        List<country> countries = CountryLoader.loadCountries();
        countryChoicebox.getItems().addAll(countries);
        countryChoicebox.setOnAction(this::setCountryChoicebox);
    }

    public void setCountryChoicebox(ActionEvent event1) {
        country selected = countryChoicebox.getValue();
        if (selected == null) {
            checkLogin.setText("Please choose a Country");
        } else {
            Ccode.setText(selected.getCode());
        }
    }


    //----------------------------//


    @FXML
    private TextField NumberText;
    @FXML
    private Button LoginButton;

    public static int PassCode;

    public void Login(ActionEvent event) {
        try {
            while (true) {

                String digits = NumberText.getText();

                digits = digits.replaceAll("\\D", "");

                if (digits.length() != 10) {
                    checkLogin.setText("Invalid Phone Number\nTry again" + digits.length());
                    continue;
                }
                    checkLogin.setText("Login Successful");
                    Thread.sleep(1500);
                    Random random = new Random();
                    PassCode = (random.nextInt(9000)+1000);
                    System.out.println(PassCode);
                    SwitchToVerificationPage(event);

                    break;

            }
        }
        catch (NumberFormatException e) {
            checkLogin.setText("Invalid Phone Number\nTry again");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //----------------------------//

    private Scene scene;
    private Stage stage;
    private Parent root;

    public void SwitchToVerificationPage(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("VerificationPage.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

}
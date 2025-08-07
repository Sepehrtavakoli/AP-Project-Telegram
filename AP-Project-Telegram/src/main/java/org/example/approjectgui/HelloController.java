package org.example.approjectgui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import org.example.model.country;
import org.example.util.CountryLoader;

public class HelloController implements Initializable {

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


    int PhoneNumber;

    public void Login(ActionEvent event) {
        try {
            PhoneNumber = Integer.parseInt(NumberText.getText());

            int digits = String.valueOf(PhoneNumber).length();

            if (digits < 9)
                checkLogin.setText("Invalid Phone Number\nTry again");
            else
                checkLogin.setText("Login Successful");
        }
        catch (NumberFormatException e) {
            checkLogin.setText("Invalid Phone Number\nTry again");
        }
    }
}
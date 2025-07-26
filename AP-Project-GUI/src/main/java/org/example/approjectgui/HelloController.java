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
import java.util.ResourceBundle;

public class HelloController implements Initializable {

    public ImageView logoImage;

    @FXML
    private ChoiceBox<String> countryChoicebox;
    private String[] countries = {"Afghanistan          +93", "Albania           +253", "Algeria           +303","Iran          +98"};

    @FXML
    private Label Ccode;

    @FXML
    private Label checkLogin;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        countryChoicebox.getItems().addAll(countries);
        countryChoicebox.setOnAction(this::setCountryChoicebox);
    }

    public void setCountryChoicebox(ActionEvent event1) {
        String CountryCode;
        switch (countryChoicebox.getValue()) {
            case "Afghanistan          +93":
                CountryCode = "+93";
                break;
            case "Albania           +253":
                CountryCode = "+253";
                break;
            case "Algeria           +303":
                CountryCode = "+303";
                break;
            case "Iran          +98":
                CountryCode = "+98";
                break;
            default:
                CountryCode = "--";
        }
        if (CountryCode.equals("--")) {
            checkLogin.setText("Please Choose a Code First !");
        }
        else {
            Ccode.setText(CountryCode);
        }
    }




    @FXML
    private TextField NumberText;
    @FXML
    private Button LoginButton;


    int PhoneNumber;

    public void Login(ActionEvent event) {
        try {
            PhoneNumber = Integer.parseInt(NumberText.getText());
            int digits = 0;
            while (true)
            {
                PhoneNumber = PhoneNumber / 10;
                digits++;
                if (PhoneNumber == 0)
                    break;
                else if (digits > 9)
                {
                    checkLogin.setText("Invalid Phone Number\nTry again");
                    break;
                }
            }
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
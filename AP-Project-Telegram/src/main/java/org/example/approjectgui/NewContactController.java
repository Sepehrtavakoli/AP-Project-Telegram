package org.example.approjectgui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.projectbackend.Contact;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewContactController implements Initializable {

    @FXML
    private ChoiceBox<String> NumType;

    private String[] numberType = {"Mobile","Email","Home","Work"};

    @FXML
    private TextField firstNameTextField;
    @FXML
    private TextField lastNameTextField;
    @FXML
    private TextField PhoneNumber;

    @FXML
    private Label firstLetter;


    public void initialize(URL location, ResourceBundle resources) {
        NumType.getItems().addAll(numberType);
        NumType.setOnAction(this::getNumType);

        setupFirstLetterDisplay();
    }

    @FXML
    private Button CreatNewContact;

    public void getNumType(ActionEvent event) {
        String numType = NumType.getValue();
    }

    public void setupFirstLetterDisplay() {
        firstNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                char firstChar = newValue.charAt(0);
                firstLetter.setText(String.valueOf(firstChar).toUpperCase());
            } else {
                firstLetter.setText("");
            }
        });
    }

    @FXML
    private Label AlertLabel;

    public void createNewContact(ActionEvent event) {

        String firstName = firstNameTextField.getText();
        String lastName = lastNameTextField.getText();
        String phoneNumber = PhoneNumber.getText();

        if (!firstName.isEmpty() && !phoneNumber.isEmpty()) {
            phoneNumber = phoneNumber.replaceAll("\\D", "");
            if (phoneNumber.length() == 10) {
                Contact contact = new Contact();
                contact.setUserData(phoneNumber, firstName, lastName);
            }
            else {
                AlertLabel.setText("Invalid Phone Number");
            }
        }else{
            AlertLabel.setText("Empty Name");
        }

    }



    @FXML
    private ImageView imageView;

    private Stage stage;
    private Scene scene;
    private Parent root;

    public void BackArrow(javafx.scene.input.MouseEvent mouseEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("ContactPage.fxml"));
        stage = (Stage)((Node)mouseEvent.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}



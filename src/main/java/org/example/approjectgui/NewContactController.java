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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.projectbackend.Contact;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewContactController implements Initializable {

    @FXML
    private ChoiceBox<String> NumType1, NumType2, NumType3;

    private String[] numberType = {"Mobile", "Email", "Home", "Work"};

    @FXML private TextField firstNameTextField;
    @FXML private TextField lastNameTextField;
    @FXML private TextField Phone1, Phone2, Phone3;
    @FXML private Label firstLetter;
    @FXML private HBox phone2Container, phone3Container;
    @FXML private VBox phoneContainer;
    @FXML private Button addPhoneButton;

    private int phoneFieldCount = 1;

    public void initialize(URL location, ResourceBundle resources) {
        NumType1.getItems().addAll(numberType);
        NumType2.getItems().addAll(numberType);
        NumType3.getItems().addAll(numberType);
        NumType1.setValue("Mobile");
        NumType2.setValue("Mobile");
        NumType3.setValue("Mobile");

        setupFirstLetterDisplay();
    }

    @FXML
    private Button CreatNewContact;

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

        String phone1 = Phone1.getText();
        if (phone1.isEmpty()) {
            AlertLabel.setText("Please enter at least one phone number");
            return;
        }

        if (firstName.isEmpty()) {
            AlertLabel.setText("Please enter a first name");
            return;
        }

        phone1 = phone1.replaceAll("\\D", "");
        if (phone1.length() != 10) {
            AlertLabel.setText("Invalid Phone Number 1");
            return;
        }

        String phone2 = "";
        if (phone2Container.isVisible()) {
            phone2 = Phone2.getText().replaceAll("\\D", "");
            if (!phone2.isEmpty() && phone2.length() != 10) {
                AlertLabel.setText("Invalid Phone Number 2");
                return;
            }
        }

        String phone3 = "";
        if (phone3Container.isVisible()) {
            phone3 = Phone3.getText().replaceAll("\\D", "");
            if (!phone3.isEmpty() && phone3.length() != 10) {
                AlertLabel.setText("Invalid Phone Number 3");
                return;
            }
        }

        Contact contact = new Contact();
        contact.setUserData(phone1, firstName, lastName);

        AlertLabel.setText("Contact created successfully!");

        firstNameTextField.clear();
        lastNameTextField.clear();
        Phone1.clear();
        if(Phone2 != null) Phone2.clear();
        if(Phone3 != null) Phone3.clear();

        resetPhoneFields();
    }

    @FXML
    public void addPhoneField(ActionEvent event) {
        if (phoneFieldCount < 3) {
            phoneFieldCount++;

            if (phoneFieldCount == 2) {
                phone2Container.setVisible(true);
                phone2Container.setManaged(true);
            } else if (phoneFieldCount == 3) {
                phone3Container.setVisible(true);
                phone3Container.setManaged(true);
                addPhoneButton.setVisible(false);
                addPhoneButton.setManaged(false);
            }
        }
    }

    private void resetPhoneFields() {
        phoneFieldCount = 1;
        phone2Container.setVisible(false);
        phone2Container.setManaged(false);
        phone3Container.setVisible(false);
        phone3Container.setManaged(false);
        addPhoneButton.setVisible(true);
        addPhoneButton.setManaged(true);
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
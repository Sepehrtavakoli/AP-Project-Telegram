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

public class NewContactController implements Initializable {

    @FXML private ChoiceBox<String> NumType1, NumType2, NumType3;
    @FXML private TextField firstNameTextField, lastNameTextField;
    @FXML private TextField Phone1, Phone2, Phone3;
    @FXML private Label firstLetter, AlertLabel;
    @FXML private HBox phone2Container, phone3Container;
    @FXML private VBox phoneContainer;
    @FXML private Button addPhoneButton, CreatNewContact;

    private final String[] numberTypes = {"Mobile", "Email", "Home", "Work"};
    private int phoneFieldCount = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // ChoiceBox init
        NumType1.getItems().addAll(numberTypes);
        NumType2.getItems().addAll(numberTypes);
        NumType3.getItems().addAll(numberTypes);
        NumType1.setValue("Mobile");
        NumType2.setValue("Mobile");
        NumType3.setValue("Mobile");

        setupFirstLetterDisplay();
    }

    private void setupFirstLetterDisplay() {
        firstNameTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                firstLetter.setText(String.valueOf(newVal.charAt(0)).toUpperCase());
            } else {
                firstLetter.setText("");
            }
        });
    }

    @FXML
    private void createNewContact(ActionEvent event) {
        String firstName = firstNameTextField.getText().trim();
        String lastName = lastNameTextField.getText().trim();
        String phone1 = Phone1.getText().trim();

        if (!isValidPhone(phone1)) {
            showAlert("Phone number 1 must be exactly 10 digits", false);
            return;
        }

        if (phone2Container.isVisible() && !Phone2.getText().isEmpty() && !isValidPhone(Phone2.getText())) {
            showAlert("Phone number 2 must be exactly 10 digits", false);
            return;
        }

        if (phone3Container.isVisible() && !Phone3.getText().isEmpty() && !isValidPhone(Phone3.getText())) {
            showAlert("Phone number 3 must be exactly 10 digits", false);
            return;
        }


        // Validate optional phones
        if (phone2Container.isVisible() && !Phone2.getText().isEmpty() && !isValidPhone(Phone2.getText())) {
            showAlert("Invalid Phone Number 2", false);
            return;
        }
        if (phone3Container.isVisible() && !Phone3.getText().isEmpty() && !isValidPhone(Phone3.getText())) {
            showAlert("Invalid Phone Number 3", false);
            return;
        }

        // ساختن کانتکت
        Contact contact = new Contact();
        contact.setUserData(phone1, firstName, lastName);
        // TODO: ذخیره‌سازی در DatabaseHelper

        showAlert("Contact created successfully!", true);
        resetForm();
    }

    private boolean isValidPhone(String number) {
        return number.matches("\\d{10}"); // دقیقا ۱۰ رقم
    }


    private void showAlert(String msg, boolean success) {
        AlertLabel.setText(msg);
        AlertLabel.setStyle(success ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
    }

    @FXML
    private void addPhoneField(ActionEvent event) {
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

    private void resetForm() {
        firstNameTextField.clear();
        lastNameTextField.clear();
        Phone1.clear();
        if (Phone2 != null) Phone2.clear();
        if (Phone3 != null) Phone3.clear();
        firstLetter.setText("");
        resetPhoneFields();
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
    private void BackArrow(javafx.scene.input.MouseEvent mouseEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("ContactPage.fxml"));
        Stage stage = (Stage)((Node)mouseEvent.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}

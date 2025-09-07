package org.example.approjectgui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.database.DatabaseHelper;
import org.example.model.User;
import org.example.model.country;
import org.example.projectbackend.Contact;
import org.example.util.CountryLoader;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class NewContactController implements Initializable {

    @FXML private TextField firstNameTextField;
    @FXML private TextField lastNameTextField;
    @FXML private TextField Phone1;
    @FXML private Label firstLetter;
    @FXML private Label AlertLabel;
    @FXML private ComboBox<country> countryCodeBox1;

    private List<country> countries;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupFirstLetterDisplay();
        setupCountryCodeBox();
    }

    private void setupFirstLetterDisplay() {
        firstNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                char firstChar = newValue.charAt(0);
                firstLetter.setText(String.valueOf(firstChar).toUpperCase());
            } else {
                firstLetter.setText("");
            }
        });
    }

    private void setupCountryCodeBox() {
        countries = CountryLoader.loadCountries();
        countryCodeBox1.getItems().addAll(countries);

        country defaultCountry = countries.stream()
                .filter(c -> "Iran".equalsIgnoreCase(c.getName()) || "IR".equalsIgnoreCase(c.getName()))
                .findFirst()
                .orElse(countries.get(0));

        countryCodeBox1.setValue(defaultCountry);

        countryCodeBox1.setCellFactory(param -> new ListCell<country>() {
            @Override
            protected void updateItem(country item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getCode() + " " + item.getName());
                }
            }
        });

        countryCodeBox1.setButtonCell(new ListCell<country>() {
            @Override
            protected void updateItem(country item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getCode());
                }
            }
        });
    }

    // In the NewContactController.java file
    @FXML
    public void createNewContact(ActionEvent event) {
        String firstName = firstNameTextField.getText().trim();
        String lastName = lastNameTextField.getText().trim();
        country selectedCountry = countryCodeBox1.getValue();
        String phoneDigits = Phone1.getText().replaceAll("\\D", "");

        if (selectedCountry == null) {
            AlertLabel.setText("Please select a country code");
            return;
        }

        if (firstName.isEmpty()) {
            AlertLabel.setText("Please enter a first name");
            return;
        }

        if (phoneDigits.isEmpty()) {
            AlertLabel.setText("Please enter a phone number");
            return;
        }

        if (phoneDigits.length() != 10) {
            AlertLabel.setText("Phone number must be 10 digits");
            return;
        }

        // This is the key change: We use the 10-digit number for the database search
        String phoneNumberForSearch = phoneDigits;

        // Step 1: Search for the user in the entire database
        User existingUser = DatabaseHelper.getUserByPhone(phoneNumberForSearch);

        if (existingUser == null) {
            AlertLabel.setText("User with this phone number does not exist.");
            return;
        }

        // Step 2: If the user exists, check if they are already in the contacts list
        if (DatabaseHelper.contactExists(UserData.currentUser.getUserId(), phoneNumberForSearch)) {
            AlertLabel.setText("This contact already exists");
            return;
        }

        // Step 3: Create the contact with the existing user's details
        Contact newContact = new Contact();
        newContact.setFirstName(firstName);
        newContact.setLastName(lastName);
        newContact.setPhoneNumber(selectedCountry.getCode() + phoneDigits); // Save the full number for display
        newContact.setContactUser(existingUser);

        boolean success = DatabaseHelper.addContact(newContact, UserData.currentUser.getUserId());

        if (success) {
            AlertLabel.setText("Contact created successfully!");
            firstNameTextField.clear();
            lastNameTextField.clear();
            Phone1.clear();

            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    Platform.runLater(() -> {
                        try {
                            Parent root = FXMLLoader.load(getClass().getResource("ContactPage.fxml"));
                            Stage currentStage = (Stage) AlertLabel.getScene().getWindow();
                            Scene scene = new Scene(root);
                            currentStage.setScene(scene);
                            currentStage.show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            AlertLabel.setText("Error adding contact");
        }
    }

    @FXML
    public void BackArrow(javafx.scene.input.MouseEvent mouseEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("ContactPage.fxml"));
        Stage currentStage;

        if (mouseEvent != null) {
            currentStage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
        } else {
            currentStage = (Stage) AlertLabel.getScene().getWindow();
        }

        Scene scene = new Scene(root);
        currentStage.setScene(scene);
        currentStage.show();
    }
}
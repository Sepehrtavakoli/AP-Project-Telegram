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
import org.example.database.DatabaseHelper;
import org.example.model.User;
import org.example.projectbackend.Contact;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.regex.Pattern;

public class NewContactController implements Initializable {

    @FXML private TextField firstNameTextField;
    @FXML private TextField lastNameTextField;
    @FXML private TextField Phone1;
    @FXML private Label firstLetter;
    @FXML private HBox phone2Container, phone3Container;
    @FXML private VBox phoneContainer;
    @FXML private Button addPhoneButton;
    @FXML private Label AlertLabel;

    private int phoneFieldCount = 1;
    private Stage stage;
    private Scene scene;
    private Parent root;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupFirstLetterDisplay();
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

    @FXML
    public void createNewContact(ActionEvent event) {
        String firstName = firstNameTextField.getText().trim();
        String lastName = lastNameTextField.getText().trim();
        String phone1 = Phone1.getText().replaceAll("\\D", "");

        // اعتبارسنجی
        if (firstName.isEmpty()) {
            AlertLabel.setText("Please enter a first name");
            return;
        }

        if (phone1.isEmpty()) {
            AlertLabel.setText("Please enter a phone number");
            return;
        }

        if (phone1.length() != 10) {
            AlertLabel.setText("Phone number must be 10 digits");
            return;
        }

        // بررسی اینکه آیا این مخاطب قبلاً اضافه شده
        if (DatabaseHelper.contactExists(UserData.currentUser.getUserId(), phone1)) {
            AlertLabel.setText("This contact already exists");
            return;
        }

        // ایجاد کاربر برای مخاطب
        User contactUser = new User(firstName, null, phone1);
        contactUser.setLastName(lastName);

        // ذخیره مخاطب در دیتابیس
        boolean success = DatabaseHelper.addContact(UserData.currentUser.getUserId(), contactUser, phone1);

        if (success) {
            AlertLabel.setText("Contact created successfully!");

            // پاک کردن فیلدها
            firstNameTextField.clear();
            lastNameTextField.clear();
            Phone1.clear();
            resetPhoneFields();

            // بازگشت به صفحه مخاطبان بعد از 1 ثانیه
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("ContactPage.fxml"));
                Parent root = loader.load();

                // گرفتن کنترلر صفحه مخاطبان و رفرش لیست
                ContactController contactController = loader.getController();
                contactController.refreshContactsList();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            AlertLabel.setText("Error creating contact");

        }
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
    public void BackArrow(javafx.scene.input.MouseEvent mouseEvent) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("ContactPage.fxml"));
        stage = (Stage)((Node)(mouseEvent != null ? mouseEvent.getSource() : AlertLabel)).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
}
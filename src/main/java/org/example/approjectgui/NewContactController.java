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
    @FXML private VBox phoneContainer;
    @FXML private Label AlertLabel;

    @FXML private ComboBox<country> countryCodeBox1;

    private Stage stage;
    private Scene scene;
    private Parent root;

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
        // بارگذاری کشورها
        countries = CountryLoader.loadCountries();

        // پر کردن ComboBox با کشورها
        countryCodeBox1.getItems().addAll(countries);

        // تنظیم ایران به عنوان پیش‌فرض
        country defaultCountry = countries.stream()
                .filter(c -> "Iran".equalsIgnoreCase(c.getName()) || "IR".equalsIgnoreCase(c.getName()))
                .findFirst()
                .orElse(countries.get(0)); // اگر ایران پیدا نشد، اولین کشور را انتخاب کن

        countryCodeBox1.setValue(defaultCountry);

        // تنظیم نمایش مناسب برای کشورها
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

    @FXML
    public void createNewContact(ActionEvent event) {
        String firstName = firstNameTextField.getText().trim();
        String lastName = lastNameTextField.getText().trim();

        // گرفتن پیش‌شماره و شماره تلفن
        country selectedCountry = countryCodeBox1.getValue();
        String phoneDigits = Phone1.getText().replaceAll("\\D", "");

        if (selectedCountry == null) {
            AlertLabel.setText("Please select a country code");
            return;
        }

        // اعتبارسنجی
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

        // ایجاد شماره تلفن کامل با پیش‌شماره
        String fullPhoneNumber = selectedCountry.getCode().replace("+", "") + phoneDigits;

        // بررسی اینکه آیا این مخاطب قبلاً اضافه شده
        if (DatabaseHelper.contactExists(UserData.currentUser.getUserId(), fullPhoneNumber)) {
            AlertLabel.setText("This contact already exists");
            return;
        }

        // ایجاد کاربر برای مخاطب
        User contactUser = new User(firstName, null, fullPhoneNumber);
        contactUser.setLastName(lastName);

        // ذخیره مخاطب در دیتابیس
        boolean success = DatabaseHelper.addContact(UserData.currentUser.getUserId(), contactUser, fullPhoneNumber);

        if (success) {
            AlertLabel.setText("Contact created successfully!");

            // پاک کردن فیلدها
            firstNameTextField.clear();
            lastNameTextField.clear();
            Phone1.clear();

            // بازنشانی ComboBox به پیش‌فرض
            country defaultCountry = countries.stream()
                    .filter(c -> "Iran".equalsIgnoreCase(c.getName()) || "IR".equalsIgnoreCase(c.getName()))
                    .findFirst()
                    .orElse(countries.get(0));
            countryCodeBox1.setValue(defaultCountry);

            // بازگشت به صفحه مخاطبان بعد از 1 ثانیه
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    javafx.application.Platform.runLater(() -> {
                        try {
                            BackArrow(null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            AlertLabel.setText("Error creating contact");
        }
    }

    // حذف متد addPhoneField چون دیگر نیاز نیست

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
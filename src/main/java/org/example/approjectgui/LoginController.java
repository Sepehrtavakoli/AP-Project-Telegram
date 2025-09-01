package org.example.approjectgui;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import org.example.model.country;
import org.example.util.CountryLoader;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class LoginController implements Initializable {

    @FXML private ChoiceBox<country> countryChoicebox;
    @FXML private Label countryCodeLabel;
    @FXML private TextField phoneNumberField;
    @FXML private Label errorLabel;
    @FXML private Button nextButton;
    @FXML private ImageView loadingIndicator;

    private List<country> countries;
    public static int PassCode;

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d+$");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupCountrySelection();
        setupPhoneNumberValidation();
        setupButtonAction();
        setupPhoneNumberLimiter();
    }

    private void setupCountrySelection() {
        countries = CountryLoader.loadCountries();
        countryChoicebox.getItems().addAll(countries);

        // Set default selection to Iran
        countries.stream()
                .filter(c -> "IR".equalsIgnoreCase(c.getCode()) || "Iran".equalsIgnoreCase(c.getName()))
                .findFirst()
                .ifPresent(countryChoicebox::setValue);

        countryChoicebox.setOnAction(event -> updateCountryCode());
        updateCountryCode();
    }

    private void updateCountryCode() {
        country selected = countryChoicebox.getValue();
        if (selected != null) {
            countryCodeLabel.setText("+" + selected.getCode());
        } else {
            countryCodeLabel.setText("+98"); // Default to Iran
        }
    }

    private void setupPhoneNumberLimiter() {
        phoneNumberField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            String currentText = phoneNumberField.getText();
            if (currentText.replaceAll("\\D", "").length() >= 10 && !event.getCharacter().matches("\\D")) {
                event.consume(); // Prevent typing if already 10 digits
            }
        });
    }

    private void setupPhoneNumberValidation() {
        phoneNumberField.textProperty().addListener((observable, oldValue, newValue) -> {
            String digitsOnly = newValue.replaceAll("\\D", "");

            if (!digitsOnly.isEmpty()) {
                hideError();
                nextButton.setDisable(false);

                // Auto-format phone number as user types
                if (digitsOnly.length() <= 10) {
                    String formatted = formatPhoneNumber(digitsOnly);
                    if (!formatted.equals(newValue)) {
                        phoneNumberField.setText(formatted);
                    }
                }
            } else {
                nextButton.setDisable(true);
            }
        });
    }

    private String formatPhoneNumber(String digits) {
        if (digits.length() <= 3) {
            return digits;
        } else if (digits.length() <= 6) {
            return digits.substring(0, 3) + " " + digits.substring(3);
        } else {
            return digits.substring(0, 3) + " " + digits.substring(3, 6) + " " + digits.substring(6);
        }
    }

    private void setupButtonAction() {
        nextButton.setOnAction(this::handleLogin);
    }

    private void handleLogin(ActionEvent event) {
        String phoneDigits = phoneNumberField.getText().replaceAll("\\D", "");
        country selectedCountry = countryChoicebox.getValue();

        // Validation
        if (selectedCountry == null) {
            showError("Please select your country");
            return;
        }

        if (phoneDigits.isEmpty()) {
            showError("Please enter your phone number");
            return;
        }

        if (!PHONE_PATTERN.matcher(phoneDigits).matches()) {
            showError("Phone number can only contain digits");
            return;
        }

        if (phoneDigits.length() != 10) {
            showError("Phone number must be exactly 10 digits");
            return;
        }

        // All validations passed - proceed with login
        showLoading(true);
        hideError();

        // Simulate network request
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            Platform.runLater(() -> {
                try {
                    generateAndSendVerificationCode();
                    // Switch to verification page using SceneController
                    SceneController.switchToVerification();
                } catch (IOException e) {
                    showError("Connection error. Please try again.");
                    e.printStackTrace();
                } finally {
                    showLoading(false);
                }
            });
        }, 1, TimeUnit.SECONDS);
    }

    private void generateAndSendVerificationCode() {
        Random random = new Random();
        PassCode = random.nextInt(9000) + 1000;
        System.out.println("Generated verification code: " + PassCode);
        // TODO: Replace with actual SMS API call
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);

        FadeTransition ft = new FadeTransition(Duration.millis(300), errorLabel);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    private void hideError() {
        errorLabel.setVisible(false);
    }

    private void showLoading(boolean show) {
        loadingIndicator.setVisible(show);
        nextButton.setDisable(show);
        nextButton.setText(show ? "" : "Next");
    }
}
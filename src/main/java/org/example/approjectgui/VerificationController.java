package org.example.approjectgui;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VerificationController implements Initializable {

    @FXML private TextField codeField;
    @FXML private Label errorLabel;
    @FXML private Button verifyButton;
    @FXML private ImageView loadingIndicator;
    @FXML private Label phoneNumberLabel;
    @FXML private Label resendLabel;

    private int remainingTime = 60;
    private ScheduledExecutorService timerExecutor;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupCodeValidation();
        startResendTimer();
        phoneNumberLabel.setText("+98 ••• ••• ••••");
    }

    private void setupCodeValidation() {
        codeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                codeField.setText(newValue.replaceAll("[^\\d]", ""));
            }

            if (newValue.length() > 4) {
                codeField.setText(newValue.substring(0, 4));
            }

            verifyButton.setDisable(newValue.length() != 4);
        });

        codeField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (codeField.getText().length() == 4) {
                event.consume();
            }
        });
    }

    @FXML
    private void handleVerify() {
        String enteredCode = codeField.getText().trim();

        if (enteredCode.length() != 4) {
            showError("Please enter a 4-digit code");
            return;
        }

        try {
            int code = Integer.parseInt(enteredCode);
            showLoading(true);
            hideError();

            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.schedule(() -> {
                Platform.runLater(() -> {
                    if (code == LoginController.PassCode) {
                        // SUCCESS - Navigate to home page
                        showError("Verification successful!");
                        try {
                            // Wait a bit before navigating
                            ScheduledExecutorService navExecutor = Executors.newSingleThreadScheduledExecutor();
                            navExecutor.schedule(() -> {
                                Platform.runLater(() -> {
                                    try {
                                        SceneController.switchToCreatAccountPage();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });
                            }, 1, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        showError("Invalid verification code. Please try again.");
                    }
                    showLoading(false);
                });
            }, 1, TimeUnit.SECONDS);

        } catch (NumberFormatException e) {
            showError("Please enter a valid 4-digit code");
        }
    }

    @FXML
    private void handleResend() {
        if (remainingTime > 0) return;

        showLoading(true);
        hideError();

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            Platform.runLater(() -> {
                java.util.Random random = new java.util.Random();
                LoginController.PassCode = random.nextInt(9000) + 1000;
                System.out.println("New code sent: " + LoginController.PassCode);

                showError("New code sent to your phone");
                startResendTimer();
                showLoading(false);
            });
        }, 1, TimeUnit.SECONDS);
    }

    @FXML
    private void handleBack() {
        try {
            SceneController.switchToLogin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startResendTimer() {
        remainingTime = 60;
        resendLabel.setDisable(true);
        resendLabel.setText("Resend code in 60s");

        if (timerExecutor != null) {
            timerExecutor.shutdown();
        }

        timerExecutor = Executors.newSingleThreadScheduledExecutor();
        timerExecutor.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                remainingTime--;
                if (remainingTime > 0) {
                    resendLabel.setText("Resend code in " + remainingTime + "s");
                } else {
                    resendLabel.setText("Didn't receive code? Resend");
                    resendLabel.setDisable(false);
                    timerExecutor.shutdown();
                }
            });
        }, 1, 1, TimeUnit.SECONDS);
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
        verifyButton.setDisable(show);
        verifyButton.setText(show ? "" : "Verify");
        resendLabel.setDisable(show);
    }

}
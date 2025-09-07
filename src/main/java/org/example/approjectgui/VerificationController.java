package org.example.approjectgui;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import org.example.database.DatabaseHelper;
import org.example.model.User;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

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
        if (UserData.PhoneNumber != null) {
            phoneNumberLabel.setText("+98 " + formatPhoneNumber(UserData.PhoneNumber));
        }
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

// فقط متد handleVerify تغییر می‌کند

    @FXML
    private void handleVerify(ActionEvent event) {
        String enteredCode = codeField.getText().trim();
        if (enteredCode.length() != 4) {
            showError("لطفا کد ۴ رقمی را وارد کنید");
            return;
        }

        showLoading(true);
        hideError();

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            Platform.runLater(() -> {
                try {
                    int code = Integer.parseInt(enteredCode);
                    if (code == LoginController.PassCode) {
                        User existingUser = DatabaseHelper.getUserByPhone(UserData.PhoneNumber);

                        if (existingUser != null) {
                            UserData.currentUser = existingUser;
                            System.out.println("User logged in: " + existingUser.getUserName());
                            // <<-- اتصال به سرور در اینجا برقرار می‌شود
                            org.example.API.ClientManager.connect(UserData.currentUser);
                            SceneController.switchToHomePage();
                        } else {
                            // برای کاربر جدید، اتصال بعد از ساخت اکانت برقرار خواهد شد
                            SceneController.switchToCreatAccountPage();
                        }
                    } else {
                        showError("کد وارد شده صحیح نیست. دوباره تلاش کنید.");
                    }
                } catch (NumberFormatException e) {
                    showError("لطفا یک عدد صحیح وارد کنید.");
                } catch (IOException e) {
                    showError("خطا در بارگذاری صفحه. دوباره تلاش کنید.");
                    e.printStackTrace();
                } finally {
                    showLoading(false);
                }
            });
        }, 1, TimeUnit.SECONDS);
    }

    @FXML
    private void handleResend(ActionEvent event) {
        if (remainingTime > 0) return;

        showLoading(true);
        hideError();

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            Platform.runLater(() -> {
                java.util.Random random = new java.util.Random();
                LoginController.PassCode = random.nextInt(9000) + 1000;

                PopupController.showVerificationCode(LoginController.PassCode);

                showError("کد جدید به شماره شما ارسال شد.");
                startResendTimer();
                showLoading(false);
            });
        }, 1, TimeUnit.SECONDS);
    }

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        SceneController.switchToLogin();
    }

    private void startResendTimer() {
        remainingTime = 60;
        resendLabel.setDisable(true);
        resendLabel.setText("ارسال مجدد کد در 60s");

        if (timerExecutor != null) {
            timerExecutor.shutdown();
        }

        timerExecutor = Executors.newSingleThreadScheduledExecutor();
        timerExecutor.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                remainingTime--;
                if (remainingTime > 0) {
                    resendLabel.setText("ارسال مجدد کد در " + remainingTime + "s");
                } else {
                    resendLabel.setText("کد را دریافت نکردید؟ ارسال مجدد");
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
        verifyButton.setText(show ? "" : "تایید");
        resendLabel.setDisable(show);
    }
}
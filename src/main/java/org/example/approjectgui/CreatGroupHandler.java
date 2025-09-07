package org.example.approjectgui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.database.DatabaseHelper;
import org.example.model.User;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class CreatGroupHandler implements Initializable { // <<-- Initializable را implement کن

    private Stage stage;
    private Scene scene;
    private Parent root;
    private String avatarPath;

    @FXML private ImageView Avatar;
    @FXML private Label MemberCount;
    @FXML private VBox selectedMembersVBox;
    @FXML private TextField groupNameField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        displaySelectedMembers();
    }

    private void displaySelectedMembers() {
        List<User> selectedUsers = GroupData.getTempSelectedUserObjects();

        if (selectedUsers == null || selectedUsers.isEmpty()) {
            MemberCount.setText("0 members");
            return;
        }

        // 3. تعداد اعضا را در Label نمایش بده
        int count = selectedUsers.size();
        MemberCount.setText(count + " members");

        // 4. VBox مربوط به نمایش اعضا را پاک کن و آیتم‌های جدید اضافه کن
        selectedMembersVBox.getChildren().clear();

        for (User user : selectedUsers) {
            // برای هر کاربر یک HBox (مشابه صفحه مخاطبین) ایجاد کن
            HBox memberItem = new HBox();
            memberItem.setAlignment(Pos.CENTER_LEFT);
            memberItem.setStyle("-fx-padding: 10;");
            memberItem.setPrefWidth(selectedMembersVBox.getPrefWidth() - 20); // برای اسکرول بهتر

            String firstName = user.getFirstName() != null ? user.getFirstName() : "?";
            String avatarText = firstName.substring(0, 1).toUpperCase();
            Label avatarLabel = new Label(avatarText);
            avatarLabel.setFont(Font.font("Arial Bold", 14));
            avatarLabel.setTextFill(Color.WHITE);
            avatarLabel.setStyle("-fx-background-color: #0088cc; -fx-background-radius: 15; -fx-min-width: 30; -fx-min-height: 30; -fx-alignment: center;");

            VBox textBox = new VBox(2);
            textBox.setPadding(new Insets(0, 0, 0, 10));

            Label nameLabel = new Label(user.getUserName());
            nameLabel.setFont(Font.font("Arial", 12));
            nameLabel.setTextFill(Color.BLACK);

            Label phoneLabel = new Label(user.getPhoneNumber());
            phoneLabel.setFont(Font.font("Arial", 10));
            phoneLabel.setTextFill(Color.GRAY);

            textBox.getChildren().addAll(nameLabel, phoneLabel);
            memberItem.getChildren().addAll(avatarLabel, textBox);

            // آیتم را به VBox اضافه کن
            selectedMembersVBox.getChildren().add(memberItem);
        }
    }

    // بقیه متدها بدون تغییر می‌مانند
    @FXML
    private void BackArrow(MouseEvent event) throws IOException {
        // قبل از بازگشت، داده‌های موقت را پاک کن (اختیاری)
        // GroupData.clearData();
        Parent root = FXMLLoader.load(getClass().getResource("CreatNewGroup.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    public void SetAvatar(MouseEvent mouseEvent) throws IOException {
        // ... کد قبلی بدون تغییر
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Avatar");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File SelectedFile = fileChooser.showOpenDialog(Avatar.getScene().getWindow());

        if (SelectedFile != null) {
            avatarPath = SelectedFile.getAbsolutePath();

            Image image = new Image(SelectedFile.toURI().toString());
            Avatar.setImage(image);

            Avatar.setFitHeight(150);
            Avatar.setFitWidth(150);
            Avatar.setPreserveRatio(true);

            Circle circle = new Circle(75, 75, 75);
            Avatar.setClip(circle);
        }
    }


    @FXML
    private void createGroupFinal(MouseEvent event) throws IOException { // throws IOException اضافه شد
        String groupName = groupNameField.getText().trim();
        if (groupName.isEmpty()) {
            System.out.println("Group name cannot be empty!");
            // بهتر است یک Alert به کاربر نشان داده شود
            return;
        }

        // 2. گرفتن لیست اعضا از GroupData
        List<UUID> allMembers = GroupData.getTempSelectedMembers();
        if (allMembers == null || allMembers.isEmpty()) {
            System.out.println("No members selected!");
            return;
        }
        // (قبلاً خود کاربر سازنده هم به لیست اضافه شده)

        // 3. فراخوانی متد createGroup در DatabaseHelper
        boolean success = DatabaseHelper.createGroup(groupName, UserData.currentUser.getUserId(), allMembers);

        // 4. بررسی نتیجه و اقدام مناسب
        if (success) {
            System.out.println("Group created successfully! Returning to home page.");
            // پاک کردن داده‌های موقت (اختیاری اما توصیه می‌شود)
            GroupData.clearData();
            // هدایت کاربر به صفحه اصلی
            Parent root = FXMLLoader.load(getClass().getResource("HomePage.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } else {
            System.out.println("Failed to create group. Please try again.");
            // نمایش یک Alert به کاربر درباره خطا
        }
    }
}
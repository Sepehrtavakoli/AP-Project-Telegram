module org.example.approjectgui {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens org.example.approjectgui to javafx.fxml;
    exports org.example.approjectgui;
}
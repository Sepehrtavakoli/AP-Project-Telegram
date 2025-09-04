module org.example.approjectgui {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.sql; // اضافه کن
    requires com.google.gson;


    opens org.example.approjectgui to javafx.fxml;
    opens org.example.model to com.google.gson, javafx.fxml;
    opens org.example.projectbackend to com.google.gson;
    opens org.example.database to javafx.fxml; // اضافه کن

    exports org.example.approjectgui;
    exports org.example.model;
    exports org.example.database;

}
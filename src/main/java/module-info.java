module org.example.finaloop {
    requires javafx.controls;
    requires javafx.fxml;

    requires javafx.graphics;
    requires javafx.base;
    requires java.sql;
    requires java.prefs;
    requires jbcrypt;
    requires org.xerial.sqlitejdbc;

    opens org.example.finaloop to javafx.fxml;
    opens org.example.finaloop.controller to javafx.fxml;

    exports org.example.finaloop;
    exports org.example.finaloop.controller;
    exports org.example.finaloop.model;
    exports org.example.finaloop.database;
    exports org.example.finaloop.service;
    exports org.example.finaloop.ui;
}
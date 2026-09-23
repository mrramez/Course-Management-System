package org.example.finaloop.controller;

import org.example.finaloop.service.Session;
import org.example.finaloop.ui.Navigator;
import org.example.finaloop.ui.Ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class WelcomingCtrl {

    @FXML
    private Button themeButton;

    @FXML
    public void initialize() {
        Ui.setupThemeButton(themeButton);
    }

    @FXML
    public void login(ActionEvent event) {
        Navigator.go("Login.fxml");
    }

    @FXML
    public void signup(ActionEvent event) {
        Navigator.go("Signup.fxml");
    }

    @FXML
    public void continueAsGuest(ActionEvent event) {
        Session.loginAsGuest();
        Navigator.go("CoursesHub.fxml");
    }

}

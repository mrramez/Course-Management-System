package org.example.finaloop.controller;

import org.example.finaloop.database.DataBaseStart;
import org.example.finaloop.service.Authentication;
import org.example.finaloop.ui.Navigator;
import org.example.finaloop.ui.Ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import static org.example.finaloop.service.Authentication.isValidEmail;
import static org.example.finaloop.service.Authentication.isValidPassword;

public class SignupCtrl {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordText;
    @FXML private Button eyeButton;
    @FXML ComboBox<String> roleCombo;
    @FXML Label statusLabel;
    @FXML Button themeButton;

    @FXML
    public void initialize(){
        Ui.setupThemeButton(themeButton);

        passwordText.textProperty().bindBidirectional(passwordField.textProperty());
        eyeButton.setGraphic(Ui.icon("eye"));

        if (roleCombo != null){
            roleCombo.getItems().addAll("admin","student","trainer");
            roleCombo.setValue("student");
        }
    }

    @FXML
    public void showPassword(ActionEvent event){
        boolean show = !passwordText.isVisible();

        passwordText.setVisible(show);
        passwordText.setManaged(show);
        passwordField.setVisible(!show);
        passwordField.setManaged(!show);

        if (show){
            eyeButton.setGraphic(Ui.icon("eye-off"));
        } else {
            eyeButton.setGraphic(Ui.icon("eye"));
        }
    }

    @FXML
    public void btnregister(ActionEvent event) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String role = roleCombo.getValue();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
            Ui.showError(statusLabel, "Please enter all request field!");
            return;
        }
        if (!isValidEmail(email)){
            Ui.showError(statusLabel, "Invalid email format! Example: user@gmail.com");
            return;
        }
        if (!isValidPassword(password)){
            Ui.showError(statusLabel, "Password must be at least 8 chars with letters and numbers!");
            return;
        }

        boolean success = DataBaseStart.registerUser(name, email, password, role);
        if (success) {
            LoginCtrl login = Navigator.go("Login.fxml");
            login.prefill(email, "Create Account is Done! ");
        } else {
            Ui.showError(statusLabel, "Wrong: email is already exist or something get wrong :(");
        }
    }

    @FXML
    public void startback(ActionEvent event){
        Navigator.go("Welcoming.fxml");
    }

    @FXML
    public void goLogin(ActionEvent event){
        Navigator.go("Login.fxml");
    }

}

package org.example.finaloop.controller;

import org.example.finaloop.database.DataBaseStart;
import org.example.finaloop.model.Admin;
import org.example.finaloop.model.ParentUsers;
import org.example.finaloop.model.Student;
import org.example.finaloop.model.Trainer;
import org.example.finaloop.service.Session;
import org.example.finaloop.ui.Navigator;
import org.example.finaloop.ui.Ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.prefs.Preferences;

public class LoginCtrl {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordText;
    @FXML
    private Button eyeButton;
    @FXML
    private CheckBox rememberCheck;
    @FXML
    private Label statusLabel;
    @FXML
    private Button themeButton;

    private static final Preferences PREFS = Preferences.userNodeForPackage(LoginCtrl.class);

    @FXML
    public void initialize(){
        Ui.setupThemeButton(themeButton);

        passwordText.textProperty().bindBidirectional(passwordField.textProperty());
        eyeButton.setGraphic(Ui.icon("eye"));

        String savedEmail = PREFS.get("rememberedEmail", "");
        if (!savedEmail.isEmpty()){
            emailField.setText(savedEmail);
            rememberCheck.setSelected(true);
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

    public void prefill(String email, String message){
        emailField.setText(email);
        Ui.showSuccess(statusLabel, message);
    }

    @FXML
    public void startback(ActionEvent event){
        Navigator.go("Welcoming.fxml");
    }

    @FXML
    public void goSignup(ActionEvent event){
        Navigator.go("Signup.fxml");
    }

    @FXML
    public void btnLogin(ActionEvent event){
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            Ui.showError(statusLabel, "Please enter both email and password!");
            return;
        }

        String role = DataBaseStart.loginUser(email, password);
        if (role != null){
            int userId = DataBaseStart.getUserIdByEmail(email);
            Session.login(userId, email, role);

            if (rememberCheck.isSelected()){
                PREFS.put("rememberedEmail", email);
            } else {
                PREFS.remove("rememberedEmail");
            }

            ParentUsers user = createUser(role, userId, email);
            if (user != null){
                Navigator.go(user.getHomePage());
            } else {
                Navigator.go("Welcoming.fxml");
            }
        } else {
            Ui.showError(statusLabel, "Invalid email or password!");
        }

    }

    private ParentUsers createUser(String role, int userId, String email){
        String name = DataBaseStart.getUserNameById(userId);

        switch (role.toLowerCase()){
            case "student":
                return new Student(userId, name, email);
            case "trainer":
                return new Trainer(userId, name, email);
            case "admin":
                return new Admin(userId, name, email);
            default:
                return null;
        }
    }

}

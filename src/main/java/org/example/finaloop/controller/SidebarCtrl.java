package org.example.finaloop.controller;

import org.example.finaloop.database.DataBaseStart;
import org.example.finaloop.service.Session;
import org.example.finaloop.ui.Navigator;
import org.example.finaloop.ui.Ui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SidebarCtrl {

    @FXML private VBox navBox;
    @FXML private Label sectionLabel;
    @FXML private Label userNameLabel;
    @FXML private Label roleLabel;
    @FXML private Label avatarLabel;
    @FXML private Button themeButton;

    @FXML
    public void initialize() {
        Ui.setupThemeButton(themeButton);

        if (Session.isGuest()) {
            setupGuest();
            return;
        }

        String role = Session.getCurrentRole() == null ? "" : Session.getCurrentRole().toLowerCase();
        String name = DataBaseStart.getUserNameById(Session.getCurrentUserId());

        userNameLabel.setText(name);
        roleLabel.setText(Ui.capitalize(role));
        avatarLabel.setText(Ui.initials(name));

        switch (role) {
            case "admin":
                sectionLabel.setText("ADMIN");
                addItem("Dashboard", "dashboard", "AdminDashboard.fxml");
                addItem("Courses", "book", "CoursesList.fxml");
                addItem("Add Course", "plus", "AddCourse.fxml");
                break;
            case "trainer":
                sectionLabel.setText("TRAINER");
                addItem("Dashboard", "dashboard", "TrainerDashboard.fxml");
                addItem("My Students", "users", "MyStudents.fxml");
                addItem("Attendance", "calendar", "Attendance.fxml");
                break;
            default:
                sectionLabel.setText("STUDENT");
                addItem("Browse Courses", "compass", "CoursesHub.fxml");
                addItem("My Courses", "book", CoursesHubCtrl.MY_COURSES_PAGE);
                break;
        }
    }

    private void setupGuest() {
        userNameLabel.setText("Guest");
        roleLabel.setText("Not logged in");
        avatarLabel.setText("G");
        sectionLabel.setText("GUEST");
        addItem("Browse Courses", "compass", "CoursesHub.fxml");

        Label title = new Label("You are a guest");
        title.getStyleClass().add("user-name");
        Label text = new Label("Create an account to enroll in courses.");
        text.getStyleClass().add("field-hint");
        text.setWrapText(true);

        Button createAccount = new Button("Sign up");
        createAccount.getStyleClass().addAll("btn-primary", "btn-small");
        createAccount.setMaxWidth(Double.MAX_VALUE);
        createAccount.setOnAction(e -> Navigator.go("Signup.fxml"));

        Button signIn = new Button("Login");
        signIn.getStyleClass().add("btn-small");
        signIn.setMaxWidth(Double.MAX_VALUE);
        signIn.setOnAction(e -> Navigator.go("Login.fxml"));

        VBox card = new VBox(8, title, text, createAccount, signIn);
        card.getStyleClass().add("guest-card");
        VBox.setMargin(card, new Insets(14, 0, 0, 0));
        navBox.getChildren().add(card);
    }

    private void addItem(String text, String icon, String page) {
        Button button = new Button(text, Ui.icon(icon));
        button.getStyleClass().add("nav-button");
        button.setMaxWidth(Double.MAX_VALUE);

        if (page.equals(Navigator.getActivePage())) {
            button.getStyleClass().add("nav-active");
        }

        button.setOnAction(e -> openPage(page));
        navBox.getChildren().add(button);
    }

    private void openPage(String page) {
        String fxmlFile = page.contains("#") ? page.substring(0, page.indexOf('#')) : page;
        Navigator.go(fxmlFile, page);
    }

    @FXML
    public void logout() {
        Session.logout();
        Navigator.go("Welcoming.fxml");
    }
}

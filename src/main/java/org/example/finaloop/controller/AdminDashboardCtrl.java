package org.example.finaloop.controller;

import org.example.finaloop.database.DataBaseStart;
import org.example.finaloop.model.Course;
import org.example.finaloop.service.Session;
import org.example.finaloop.ui.CourseCover;
import org.example.finaloop.ui.Navigator;
import org.example.finaloop.ui.Ui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class AdminDashboardCtrl {

    @FXML private Label welcomeLabel;
    @FXML private Label coursesCountLabel;
    @FXML private Label trainersCountLabel;
    @FXML private Label studentsCountLabel;
    @FXML private Label enrollmentsCountLabel;
    @FXML private VBox recentBox;

    @FXML
    public void initialize() {
        String name = DataBaseStart.getUserNameById(Session.getCurrentUserId());
        welcomeLabel.setText("Welcome, " + name);

        List<Course> courses = DataBaseStart.getAllCourses();
        coursesCountLabel.setText(String.valueOf(courses.size()));
        trainersCountLabel.setText(String.valueOf(DataBaseStart.countUsersByRole("trainer")));
        studentsCountLabel.setText(String.valueOf(DataBaseStart.countUsersByRole("student")));
        enrollmentsCountLabel.setText(String.valueOf(DataBaseStart.countEnrollments()));

        showRecentCourses(courses);
    }

    private void showRecentCourses(List<Course> courses) {
        recentBox.getChildren().clear();

        if (courses.isEmpty()) {
            Button add = new Button("Add Course");
            add.getStyleClass().add("btn-primary");
            add.setOnAction(e -> addCourse());
            recentBox.getChildren().add(Ui.emptyState("No courses yet",
                    "Click Add Course to add the first course.", add));
            return;
        }

        for (int i = 0; i < Math.min(5, courses.size()); i++) {
            Course course = courses.get(i);

            StackPane thumb = CourseCover.build(course.getId(), course.getName(), course.getImagePath(),
                    56, 38, CourseCover.THUMB);

            Label name = new Label(course.getName());
            name.getStyleClass().add("user-name");
            String trainer = course.getTrainerName() != null ? course.getTrainerName() : "No trainer";
            Label meta = new Label(trainer + "  ·  " + course.getStudentsCount()
                    + (course.getStudentsCount() == 1 ? " student" : " students"));
            meta.getStyleClass().add("course-meta");
            VBox info = new VBox(2, name, meta);
            HBox.setHgrow(info, Priority.ALWAYS);

            Label price = new Label(Ui.money(course.getPrice()));
            price.getStyleClass().add("user-name");

            HBox row = new HBox(12, thumb, info, price);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getStyleClass().add("list-row");
            recentBox.getChildren().add(row);
        }
    }

    @FXML
    public void addCourse() {
        Navigator.go("AddCourse.fxml");
    }

    @FXML
    public void viewCourses() {
        Navigator.go("CoursesList.fxml");
    }
}

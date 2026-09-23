package org.example.finaloop.controller;

import org.example.finaloop.database.DataBaseStart;
import org.example.finaloop.model.Course;
import org.example.finaloop.model.Trainer;
import org.example.finaloop.service.Session;
import org.example.finaloop.ui.CourseCover;
import org.example.finaloop.ui.Navigator;
import org.example.finaloop.ui.Ui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TrainerDashboardCtrl {

    private static final double CARD_WIDTH = 262;

    @FXML private Label welcomeLabel;
    @FXML private Label coursesCountLabel;
    @FXML private Label studentsCountLabel;
    @FXML private Label sessionsCountLabel;
    @FXML private FlowPane coursesFlow;

    private Trainer trainer;

    @FXML
    public void initialize() {
        trainer = new Trainer(Session.getCurrentUserId(),
                DataBaseStart.getUserNameById(Session.getCurrentUserId()), Session.getCurrentEmail());

        welcomeLabel.setText("Welcome, " + trainer.getUserName());

        List<Course> courses = trainer.viewMyCourses();
        coursesCountLabel.setText(String.valueOf(courses.size()));

        Set<Integer> studentIds = new HashSet<>();
        for (Map<String, Object> row : trainer.viewMyStudents()) {
            studentIds.add((int) row.get("student_id"));
        }
        studentsCountLabel.setText(String.valueOf(studentIds.size()));
        sessionsCountLabel.setText(String.valueOf(DataBaseStart.countAttendanceSessions(trainer.getUserId())));

        showCourses(courses);
    }

    private void showCourses(List<Course> courses) {
        coursesFlow.getChildren().clear();

        if (courses.isEmpty()) {
            VBox empty = Ui.emptyState("No courses yet",
                    "The admin hasn't added any course for you yet.", null);
            empty.prefWidthProperty().bind(coursesFlow.widthProperty());
            coursesFlow.getChildren().add(empty);
            return;
        }

        for (Course course : courses) {
            coursesFlow.getChildren().add(buildCourseCard(course));
        }
    }

    private VBox buildCourseCard(Course course) {
        Label title = new Label(course.getName());
        title.getStyleClass().add("course-title");
        title.setWrapText(true);

        Label meta = new Label(course.getStudentsCount() + (course.getStudentsCount() == 1 ? " student" : " students")
                + "  ·  " + Ui.money(course.getPrice()));
        meta.getStyleClass().add("course-meta");

        Button attendanceBtn = new Button("Take attendance", Ui.icon("calendar"));
        attendanceBtn.getStyleClass().addAll("btn-primary", "btn-small");
        attendanceBtn.setOnAction(e -> {
            AttendanceCtrl controller = Navigator.go("Attendance.fxml");
            if (controller != null) {
                controller.selectCourse(course);
            }
        });

        Button studentsBtn = new Button("Students");
        studentsBtn.getStyleClass().add("btn-small");
        studentsBtn.setOnAction(e -> {
            MyStudentsCtrl controller = Navigator.go("MyStudents.fxml");
            if (controller != null) {
                controller.selectCourse(course);
            }
        });

        HBox actions = new HBox(8, attendanceBtn, studentsBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox body = new VBox(8, title, meta, actions);
        body.getStyleClass().add("course-body");

        VBox card = new VBox(CourseCover.build(course.getId(), course.getName(), course.getImagePath(),
                CARD_WIDTH, 148, CourseCover.CARD), body);
        card.getStyleClass().add("course-card");
        card.setPrefWidth(CARD_WIDTH);
        card.setMaxWidth(CARD_WIDTH);
        return card;
    }
}

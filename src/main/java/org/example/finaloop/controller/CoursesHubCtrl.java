package org.example.finaloop.controller;

import org.example.finaloop.database.DataBaseStart;
import org.example.finaloop.service.Session;
import org.example.finaloop.ui.CourseCover;
import org.example.finaloop.ui.Navigator;
import org.example.finaloop.ui.Ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CoursesHubCtrl {

    public static final String MY_COURSES_PAGE = "CoursesHub.fxml#my";

    @FXML private Button browseTabBtn;
    @FXML private Button myCoursesTabBtn;
    @FXML private HBox tabsBox;
    @FXML private HBox guestActions;
    @FXML private Label pageTitle;
    @FXML private Label pageSubtitle;
    @FXML private Label statusLabel;
    @FXML private Label resultsLabel;
    @FXML private TextField searchField;

    @FXML private VBox browsePane;
    @FXML private FlowPane coursesFlowPane;
    @FXML private VBox myCoursesPane;

    private List<Map<String, Object>> allCourses = new ArrayList<>();
    private Set<Integer> enrolledIds = new HashSet<>();

    @FXML
    public void initialize() {
        searchField.textProperty().addListener((obs, oldText, newText) -> renderCourses(allCourses));

        boolean guest = Session.isGuest();
        tabsBox.setVisible(!guest);
        tabsBox.setManaged(!guest);
        guestActions.setVisible(guest);
        guestActions.setManaged(guest);

        if (!guest && MY_COURSES_PAGE.equals(Navigator.getActivePage())) {
            openMyCourses();
        } else {
            openBrowse();
        }
    }

    @FXML
    public void showBrowseTab() {
        Navigator.go("CoursesHub.fxml");
    }

    @FXML
    public void showMyCoursesTab() {
        Navigator.go("CoursesHub.fxml", MY_COURSES_PAGE);
    }

    @FXML
    public void goSignIn(ActionEvent event) {
        Navigator.go("Login.fxml");
    }

    @FXML
    public void goSignUp(ActionEvent event) {
        Navigator.go("Signup.fxml");
    }

    private void openBrowse() {
        setTab(true);
        pageTitle.setText("Browse Courses");
        if (Session.isGuest()) {
            pageSubtitle.setText("Login or create an account to enroll in a course.");
        } else {
            pageSubtitle.setText("Choose a course and press Enroll.");
        }
        loadCourses();
    }

    private void openMyCourses() {
        setTab(false);
        pageTitle.setText("My Courses");
        pageSubtitle.setText("Your courses and payment status.");
        loadMyCourses();
    }

    private void setTab(boolean browse) {
        browsePane.setVisible(browse);
        browsePane.setManaged(browse);
        myCoursesPane.setVisible(!browse);
        myCoursesPane.setManaged(!browse);

        browseTabBtn.getStyleClass().remove("segment-active");
        myCoursesTabBtn.getStyleClass().remove("segment-active");
        if (browse) {
            browseTabBtn.getStyleClass().add("segment-active");
        } else {
            myCoursesTabBtn.getStyleClass().add("segment-active");
        }
    }

    private void loadCourses() {
        allCourses = DataBaseStart.getAllCoursesForBrowse();
        if (Session.isLoggedIn()) {
            enrolledIds = DataBaseStart.getEnrolledCourseIds(Session.getCurrentUserId());
        }
        renderCourses(allCourses);
    }

    private void renderCourses(List<Map<String, Object>> courses) {
        coursesFlowPane.getChildren().clear();

        if (courses.isEmpty()) {
            coursesFlowPane.getChildren().add(Ui.emptyState("No courses available at the moment.",
                    "Please check again later.", null));
            resultsLabel.setText("");
            return;
        }

        String search = searchField.getText().trim().toLowerCase();
        int count = 0;

        for (Map<String, Object> course : courses) {
            String text = (course.get("name") + " " + course.get("description") + " " + course.get("trainer_name")).toLowerCase();
            if (search.isEmpty() || text.contains(search)) {
                coursesFlowPane.getChildren().add(buildCourseCard(course));
                count++;
            }
        }

        resultsLabel.setText(count + " courses");
        if (count == 0) {
            coursesFlowPane.getChildren().add(Ui.emptyState("No courses found",
                    "Try another name.", null));
        }
    }

    private VBox buildCourseCard(Map<String, Object> course) {
        int courseId = (int) course.get("id");
        String name = String.valueOf(course.get("name"));
        Object descObj = course.get("description");
        String description = (descObj != null) ? String.valueOf(descObj) : "";
        double price = (double) course.get("price");
        Object trainerObj = course.get("trainer_name");
        String trainerName = (trainerObj != null) ? String.valueOf(trainerObj) : "Not assigned";
        String imagePath = (String) course.get("image_path");
        int students = (int) course.get("students_count");
        double rating = (double) course.get("avg_rating");
        int reviews = (int) course.get("reviews_count");

        StackPane cover = CourseCover.build(courseId, name, imagePath, 262, 148, CourseCover.CARD);

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("course-title");
        nameLabel.setWrapText(true);

        Label trainerLabel = new Label("Trainer: " + trainerName);
        trainerLabel.getStyleClass().add("course-meta");

        Label descLabel = new Label(description);
        descLabel.getStyleClass().add("course-desc");
        descLabel.setWrapText(true);
        descLabel.setMinHeight(34);
        descLabel.setMaxHeight(34);

        String ratingText;
        if (reviews > 0) {
            ratingText = String.format("%.1f (%d)", rating, reviews);
        } else {
            ratingText = "No reviews";
        }
        Label ratingLabel = new Label(ratingText + "  |  " + students + " students");
        ratingLabel.getStyleClass().add("course-meta");
        HBox ratingBox = new HBox(6, Ui.icon("star"), ratingLabel);
        ratingBox.getStyleClass().add("rating");
        ratingBox.setAlignment(Pos.CENTER_LEFT);

        Label priceLabel = new Label(Ui.money(price));
        priceLabel.getStyleClass().add("course-price");

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);

        Node action;
        if (enrolledIds.contains(courseId)) {
            action = Ui.badge("Enrolled", "success");
        } else {
            Button enrollBtn = new Button("Enroll");
            enrollBtn.getStyleClass().addAll("btn-primary", "btn-small");
            enrollBtn.setOnAction(e -> handleEnroll(courseId, name));
            action = enrollBtn;
        }

        HBox bottom = new HBox(8, priceLabel, space, action);
        bottom.setAlignment(Pos.CENTER_LEFT);

        VBox body = new VBox(8, nameLabel, trainerLabel, descLabel, ratingBox, bottom);
        body.getStyleClass().add("course-body");

        VBox card = new VBox(cover, body);
        card.getStyleClass().add("course-card");
        card.setPrefWidth(262);
        card.setMaxWidth(262);
        return card;
    }

    private void handleEnroll(int courseId, String courseName) {
        if (Session.isGuest()) {
            boolean yes = Ui.confirm("Create an account",
                    "You need an account to enroll in \"" + courseName + "\". Do you want to create one now?",
                    "Create account", false);
            if (yes) {
                Navigator.go("Signup.fxml");
            }
            return;
        }

        if (!Session.isLoggedIn()) {
            Ui.showError(statusLabel, "You need to be logged in as a student to enroll.");
            return;
        }

        int studentId = Session.getCurrentUserId();
        if (DataBaseStart.isAlreadyEnrolled(studentId, courseId)) {
            Ui.showError(statusLabel, "You are already enrolled in \"" + courseName + "\". Check My Courses.");
            return;
        }

        int enrollmentId = DataBaseStart.enrollInCourse(studentId, courseId);

        if (enrollmentId != -1) {
            enrolledIds.add(courseId);
            renderCourses(allCourses);
            Ui.showSuccess(statusLabel, "Enrolled in \"" + courseName + "\" successfully! Check My Courses to complete payment.");
        } else {
            Ui.showError(statusLabel, "Something went wrong while enrolling. Please try again.");
        }
    }

    private void loadMyCourses() {
        myCoursesPane.getChildren().clear();

        if (!Session.isLoggedIn()) {
            Ui.showError(statusLabel, "You need to be logged in as a student to see your courses.");
            return;
        }

        List<Map<String, Object>> rows = DataBaseStart.getMyCoursesWithDetails(Session.getCurrentUserId());

        if (rows.isEmpty()) {
            Button browseBtn = new Button("Browse Courses");
            browseBtn.getStyleClass().add("btn-primary");
            browseBtn.setOnAction(e -> showBrowseTab());
            myCoursesPane.getChildren().add(Ui.emptyState("You haven't enrolled in any course yet.",
                    "Try Browse Courses.", browseBtn));
            return;
        }

        VBox list = new VBox();
        list.getStyleClass().add("list-card");
        for (Map<String, Object> row : rows) {
            list.getChildren().add(buildMyCourseRow(row));
        }
        myCoursesPane.getChildren().add(list);
    }

    private HBox buildMyCourseRow(Map<String, Object> row) {
        int enrollmentId = (int) row.get("enrollment_id");
        int courseId = (int) row.get("course_id");
        String name = String.valueOf(row.get("name"));
        double price = (double) row.get("price");
        String paymentStatus = String.valueOf(row.get("payment_status"));
        Object trainerObj = row.get("trainer_name");
        String trainerName = (trainerObj != null) ? String.valueOf(trainerObj) : "Not assigned";
        boolean paid = "paid".equalsIgnoreCase(paymentStatus);

        StackPane thumb = CourseCover.build(courseId, name, (String) row.get("image_path"), 76, 50, CourseCover.THUMB);

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("course-title");
        Label infoLabel = new Label("Trainer: " + trainerName + "  |  " + Ui.money(price));
        infoLabel.getStyleClass().add("course-meta");
        VBox info = new VBox(3, nameLabel, infoLabel);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label status;
        if (paid) {
            status = Ui.badge("Paid", "success");
        } else {
            status = Ui.badge("Unpaid", "warning");
        }

        Button payBtn = new Button(paid ? "Paid" : "Pay");
        payBtn.getStyleClass().addAll("btn-primary", "btn-small");
        payBtn.setDisable(paid);
        payBtn.setOnAction(e -> {
            boolean success = DataBaseStart.payFees(enrollmentId);
            if (success) {
                loadMyCourses();
                Ui.showSuccess(statusLabel, "Payment completed for \"" + name + "\".");
            } else {
                Ui.showError(statusLabel, "Something went wrong while processing the payment.");
            }
        });

        Button reviewBtn = new Button("Review");
        reviewBtn.getStyleClass().add("btn-small");
        reviewBtn.setOnAction(e -> ReviewPopupCtrl.open(Session.getCurrentUserId(), courseId, name));

        HBox line = new HBox(16, thumb, info, status, payBtn, reviewBtn);
        line.getStyleClass().add("list-row");
        line.setAlignment(Pos.CENTER_LEFT);
        return line;
    }
}

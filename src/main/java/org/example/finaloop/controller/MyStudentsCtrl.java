package org.example.finaloop.controller;

import org.example.finaloop.database.DataBaseStart;
import org.example.finaloop.model.Course;
import org.example.finaloop.model.Trainer;
import org.example.finaloop.service.Session;
import org.example.finaloop.ui.Ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.util.List;
import java.util.Map;

public class MyStudentsCtrl {

    @FXML private TextField searchField;
    @FXML private ComboBox<Course> courseCombo;
    @FXML private Label countLabel;

    @FXML private TableView<StudentRow> studentsTable;
    @FXML private TableColumn<StudentRow, String> nameColumn;
    @FXML private TableColumn<StudentRow, String> emailColumn;
    @FXML private TableColumn<StudentRow, String> courseColumn;
    @FXML private TableColumn<StudentRow, String> paymentColumn;

    private final ObservableList<StudentRow> rows = FXCollections.observableArrayList();
    private final FilteredList<StudentRow> filteredRows = new FilteredList<>(rows, r -> true);
    private Trainer trainer;

    @FXML
    public void initialize() {
        trainer = new Trainer(Session.getCurrentUserId(),
                DataBaseStart.getUserNameById(Session.getCurrentUserId()), Session.getCurrentEmail());

        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        emailColumn.setCellValueFactory(data -> data.getValue().emailProperty());
        courseColumn.setCellValueFactory(data -> data.getValue().courseNameProperty());
        paymentColumn.setCellValueFactory(data -> data.getValue().paymentStatusProperty());

        nameColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) {
                    setGraphic(null);
                    return;
                }
                Label initials = new Label(Ui.initials(name));
                initials.getStyleClass().add("avatar-text");
                StackPane avatar = new StackPane(initials);
                avatar.getStyleClass().addAll("avatar", "avatar-small");
                Label nameLabel = new Label(name);
                nameLabel.getStyleClass().add("user-name");
                HBox box = new HBox(10, avatar, nameLabel);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });

        paymentColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    return;
                }
                setGraphic("paid".equalsIgnoreCase(status)
                        ? Ui.badge("Paid", "success")
                        : Ui.badge("Payment due", "warning"));
            }
        });

        studentsTable.setItems(filteredRows);
        studentsTable.setPlaceholder(Ui.emptyState("No students yet",
                "No students are enrolled in your courses yet.", null));

        courseCombo.getItems().addAll(trainer.viewMyCourses());
        courseCombo.valueProperty().addListener((obs, oldValue, newValue) -> loadStudents());
        searchField.textProperty().addListener((obs, oldValue, newValue) -> applySearch());

        loadStudents();
    }

    public void selectCourse(Course course) {
        for (Course item : courseCombo.getItems()) {
            if (item.getId() == course.getId()) {
                courseCombo.setValue(item);
            }
        }
    }

    @FXML
    public void showAllStudents() {
        searchField.clear();
        courseCombo.setValue(null);
    }

    private void loadStudents() {
        Course course = courseCombo.getValue();
        List<Map<String, Object>> data = (course == null)
                ? trainer.viewMyStudents()
                : trainer.viewMyStudents(course.getId());

        rows.clear();
        for (Map<String, Object> row : data) {
            rows.add(new StudentRow(
                    String.valueOf(row.get("name")),
                    String.valueOf(row.get("email")),
                    String.valueOf(row.get("course_name")),
                    String.valueOf(row.get("payment_status"))
            ));
        }
        applySearch();
    }

    private void applySearch() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        filteredRows.setPredicate(row -> query.isEmpty()
                || row.nameProperty().get().toLowerCase().contains(query)
                || row.emailProperty().get().toLowerCase().contains(query));
        countLabel.setText(filteredRows.size() + (filteredRows.size() == 1 ? " student" : " students"));
    }

    public static class StudentRow {
        private final SimpleStringProperty name;
        private final SimpleStringProperty email;
        private final SimpleStringProperty courseName;
        private final SimpleStringProperty paymentStatus;

        public StudentRow(String name, String email, String courseName, String paymentStatus) {
            this.name = new SimpleStringProperty(name);
            this.email = new SimpleStringProperty(email);
            this.courseName = new SimpleStringProperty(courseName);
            this.paymentStatus = new SimpleStringProperty(paymentStatus);
        }

        public SimpleStringProperty nameProperty() { return name; }
        public SimpleStringProperty emailProperty() { return email; }
        public SimpleStringProperty courseNameProperty() { return courseName; }
        public SimpleStringProperty paymentStatusProperty() { return paymentStatus; }
    }
}

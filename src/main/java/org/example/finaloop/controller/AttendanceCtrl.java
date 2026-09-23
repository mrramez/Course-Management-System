package org.example.finaloop.controller;

import org.example.finaloop.database.DataBaseStart;
import org.example.finaloop.model.Attendance;
import org.example.finaloop.model.Course;
import org.example.finaloop.model.Trainer;
import org.example.finaloop.service.Session;
import org.example.finaloop.ui.Ui;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceCtrl {

    @FXML private ComboBox<Course> courseCombo;
    @FXML private DatePicker datePicker;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private Label presentChip;
    @FXML private Label absentChip;
    @FXML private Label totalChip;
    @FXML private Label savedInfo;
    @FXML private Button saveButton;

    @FXML private TableView<AttendanceRow> attendanceTable;
    @FXML private TableColumn<AttendanceRow, String> nameColumn;
    @FXML private TableColumn<AttendanceRow, String> savedColumn;
    @FXML private TableColumn<AttendanceRow, Boolean> presentColumn;

    private final ObservableList<AttendanceRow> rows = FXCollections.observableArrayList();
    private final FilteredList<AttendanceRow> filteredRows = new FilteredList<>(rows, r -> true);
    private Trainer trainer;

    private Course loadedCourse;
    private String loadedDate;

    @FXML
    public void initialize() {
        trainer = new Trainer(Session.getCurrentUserId(),
                DataBaseStart.getUserNameById(Session.getCurrentUserId()), Session.getCurrentEmail());

        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        savedColumn.setCellValueFactory(data -> data.getValue().savedStatusProperty());

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

        savedColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    return;
                }
                switch (status) {
                    case "Present":
                        setGraphic(Ui.badge("Present", "success"));
                        break;
                    case "Absent":
                        setGraphic(Ui.badge("Absent", "danger"));
                        break;
                    default:
                        setGraphic(Ui.badge("Not taken yet", "neutral"));
                        break;
                }
            }
        });

        presentColumn.setCellValueFactory(data -> data.getValue().presentProperty());
        presentColumn.setCellFactory(CheckBoxTableCell.forTableColumn(presentColumn));
        attendanceTable.setEditable(true);
        attendanceTable.setItems(filteredRows);

        courseCombo.getItems().addAll(trainer.viewMyCourses());
        datePicker.setValue(LocalDate.now());

        courseCombo.valueProperty().addListener((obs, oldValue, newValue) -> loadStudents());
        datePicker.valueProperty().addListener((obs, oldValue, newValue) -> loadStudents());
        searchField.textProperty().addListener((obs, oldValue, newValue) -> applySearch());

        if (courseCombo.getItems().isEmpty()) {
            attendanceTable.setPlaceholder(Ui.emptyState("No courses yet",
                    "The admin hasn't added any course for you yet.", null));
        } else {
            attendanceTable.setPlaceholder(Ui.emptyState("Choose a course",
                    "Choose one of your courses to see the students.", null));
        }
        updateSummary();
    }

    public void selectCourse(Course course) {
        for (Course item : courseCombo.getItems()) {
            if (item.getId() == course.getId()) {
                courseCombo.setValue(item);
            }
        }
    }

    private void loadStudents() {
        Ui.hide(statusLabel);
        Course course = courseCombo.getValue();
        LocalDate date = datePicker.getValue();

        if (course == null || date == null) {
            clearTable();
            return;
        }
        if (date.isAfter(LocalDate.now())) {
            clearTable();
            Ui.showError(statusLabel, "You can't take attendance for a future date!");
            return;
        }

        fillTable(course, date.toString());

        if (rows.isEmpty()) {
            attendanceTable.setPlaceholder(Ui.emptyState("No students yet",
                    "No students are enrolled in this course yet.", null));
        }
    }

    private void fillTable(Course course, String date) {
        List<Attendance> sheet = trainer.getAttendanceSheet(course.getId(), date);
        List<AttendanceRow> newRows = new ArrayList<>();
        boolean takenBefore = false;

        for (Attendance a : sheet) {
            String saved;
            boolean present;
            if (a.isTaken()) {
                saved = a.isPresent() ? "Present" : "Absent";
                present = a.isPresent();
                takenBefore = true;
            } else {
                saved = "Not taken yet";
                present = true;
            }
            AttendanceRow row = new AttendanceRow(a.getStudentId(), a.getStudentName(), saved, present);
            row.presentProperty().addListener((obs, oldValue, newValue) -> updateSummary());
            newRows.add(row);
        }

        rows.setAll(newRows);
        loadedCourse = course;
        loadedDate = date;

        if (takenBefore) {
            savedInfo.setText("Saved before");
        } else {
            savedInfo.setText("Not saved yet");
        }
        applySearch();
        updateSummary();
    }

    private void clearTable() {
        rows.clear();
        loadedCourse = null;
        loadedDate = null;
        savedInfo.setText("");
        updateSummary();
    }

    private void applySearch() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        filteredRows.setPredicate(row -> query.isEmpty() || row.nameProperty().get().toLowerCase().contains(query));
    }

    private void updateSummary() {
        int present = 0;
        for (AttendanceRow row : rows) {
            if (row.isPresent()) {
                present++;
            }
        }
        presentChip.setText("Present " + present);
        absentChip.setText("Absent " + (rows.size() - present));
        totalChip.setText("Total " + rows.size());
        saveButton.setDisable(rows.isEmpty());
    }

    @FXML
    public void markAllPresent() {
        for (AttendanceRow row : rows) {
            row.setPresent(true);
        }
    }

    @FXML
    public void markAllAbsent() {
        for (AttendanceRow row : rows) {
            row.setPresent(false);
        }
    }

    @FXML
    public void saveAttendance() {
        if (loadedCourse == null || rows.isEmpty()) {
            Ui.showError(statusLabel, "Please choose a course first!");
            return;
        }

        List<Attendance> attendanceList = new ArrayList<>();
        int presentCount = 0;
        int absentCount = 0;

        for (AttendanceRow row : rows) {
            Attendance a = new Attendance();
            a.setStudentId(row.getStudentId());
            a.setCourseId(loadedCourse.getId());
            a.setAttendanceDate(loadedDate);

            if (row.isPresent()) {
                a.setStatus("present");
                presentCount++;
            } else {
                a.setStatus("absent");
                absentCount++;
            }
            attendanceList.add(a);
        }

        boolean success = trainer.markAttendance(attendanceList);
        if (success) {
            fillTable(loadedCourse, loadedDate);
            Ui.showSuccess(statusLabel, "Attendance saved! Present: " + presentCount + " | Absent: " + absentCount);
        } else {
            Ui.showError(statusLabel, "Something went wrong while saving the attendance :(");
        }
    }

    public static class AttendanceRow {
        private final int studentId;
        private final SimpleStringProperty name;
        private final SimpleStringProperty savedStatus;
        private final SimpleBooleanProperty present;

        public AttendanceRow(int studentId, String name, String savedStatus, boolean present) {
            this.studentId = studentId;
            this.name = new SimpleStringProperty(name);
            this.savedStatus = new SimpleStringProperty(savedStatus);
            this.present = new SimpleBooleanProperty(present);
        }

        public int getStudentId() { return studentId; }
        public boolean isPresent() { return present.get(); }
        public void setPresent(boolean value) { present.set(value); }
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleStringProperty savedStatusProperty() { return savedStatus; }
        public SimpleBooleanProperty presentProperty() { return present; }
    }
}

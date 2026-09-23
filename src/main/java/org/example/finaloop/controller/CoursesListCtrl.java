package org.example.finaloop.controller;

import org.example.finaloop.database.DataBaseStart;
import org.example.finaloop.model.Admin;
import org.example.finaloop.model.Course;
import org.example.finaloop.service.Session;
import org.example.finaloop.ui.CourseCover;
import org.example.finaloop.ui.Navigator;
import org.example.finaloop.ui.Ui;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Set;
import java.util.TreeSet;

public class CoursesListCtrl {

    private static final String ALL_TRAINERS = "All trainers";

    @FXML private TableView<Course> coursesTable;
    @FXML private TableColumn<Course, Course> courseColumn;
    @FXML private TableColumn<Course, String> trainerColumn;
    @FXML private TableColumn<Course, Integer> studentsColumn;
    @FXML private TableColumn<Course, String> priceColumn;
    @FXML private TableColumn<Course, Void> actionColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> trainerFilter;
    @FXML private Label countLabel;
    @FXML private Label statusLabel;

    private final ObservableList<Course> allCourses = FXCollections.observableArrayList();
    private final FilteredList<Course> filteredCourses = new FilteredList<>(allCourses, c -> true);

    @FXML
    public void initialize() {
        courseColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));
        courseColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                if (empty || course == null) {
                    setGraphic(null);
                    return;
                }
                Label name = new Label(course.getName());
                name.getStyleClass().add("user-name");
                String description = course.getDescription() == null || course.getDescription().isBlank()
                        ? "No description" : course.getDescription();
                Label desc = new Label(description);
                desc.getStyleClass().add("course-desc");
                HBox box = new HBox(12, CourseCover.build(course.getId(), course.getName(), course.getImagePath(),
                        56, 38, CourseCover.THUMB), new VBox(2, name, desc));
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });

        trainerColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getTrainerName() != null ? data.getValue().getTrainerName() : "Not assigned"));
        studentsColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getStudentsCount()).asObject());
        priceColumn.setCellValueFactory(data -> new SimpleStringProperty(Ui.money(data.getValue().getPrice())));
        addActionButtonsToTable();

        coursesTable.setItems(filteredCourses);
        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        trainerFilter.valueProperty().addListener((obs, oldValue, newValue) -> applyFilters());

        loadCourses();
    }

    private void loadCourses() {
        allCourses.setAll(DataBaseStart.getAllCourses());

        Set<String> trainers = new TreeSet<>();
        for (Course course : allCourses) {
            if (course.getTrainerName() != null) {
                trainers.add(course.getTrainerName());
            }
        }
        String selected = trainerFilter.getValue();
        trainerFilter.getItems().setAll(ALL_TRAINERS);
        trainerFilter.getItems().addAll(trainers);
        trainerFilter.setValue(selected != null && trainerFilter.getItems().contains(selected) ? selected : ALL_TRAINERS);

        applyFilters();
    }

    private void applyFilters() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String trainer = trainerFilter.getValue();

        filteredCourses.setPredicate(course -> {
            boolean matchesText = query.isEmpty()
                    || course.getName().toLowerCase().contains(query)
                    || (course.getDescription() != null && course.getDescription().toLowerCase().contains(query));
            boolean matchesTrainer = trainer == null || ALL_TRAINERS.equals(trainer)
                    || trainer.equals(course.getTrainerName());
            return matchesText && matchesTrainer;
        });

        countLabel.setText(filteredCourses.size() + " of " + allCourses.size());

        if (allCourses.isEmpty()) {
            Button add = new Button("Add Course");
            add.getStyleClass().add("btn-primary");
            add.setOnAction(e -> addCourse());
            coursesTable.setPlaceholder(Ui.emptyState("No courses yet",
                    "Click Add Course to add the first course.", add));
        } else {
            coursesTable.setPlaceholder(Ui.emptyState("No courses found",
                    "Try another name or trainer.", null));
        }
    }

    private void addActionButtonsToTable() {
        actionColumn.setCellFactory(column -> new TableCell<>() {

            private final Button editBtn = new Button("Edit", Ui.icon("edit"));
            private final Button deleteBtn = new Button("Delete", Ui.icon("trash"));
            private final HBox box = new HBox(8, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("btn-small");
                deleteBtn.getStyleClass().addAll("btn-small", "btn-danger");
                box.setAlignment(Pos.CENTER_RIGHT);

                editBtn.setOnAction(e -> openEditScreen(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> confirmAndDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void openEditScreen(Course course) {
        AddCourseCtrl controller = Navigator.go("AddCourse.fxml", "CoursesList.fxml");
        if (controller != null) {
            controller.setCourseForEdit(course);
        }
    }

    private void confirmAndDelete(Course course) {
        boolean confirmed = Ui.confirm("Delete Course",
                "Are you sure you want to delete the course \"" + course.getName() + "\"?",
                "Delete", true);
        if (!confirmed) {
            return;
        }

        Admin admin = new Admin(Session.getCurrentUserId(),
                DataBaseStart.getUserNameById(Session.getCurrentUserId()), Session.getCurrentEmail());
        if (!admin.isAuthorizedAdmin()) {
            Ui.showError(statusLabel, "Access denied: only an admin can do this!");
            return;
        }

        if (admin.deleteCourse(course.getId())) {
            loadCourses();
            Ui.showSuccess(statusLabel, "Course \"" + course.getName() + "\" deleted.");
        } else {
            Ui.showError(statusLabel, "Something went wrong while deleting the course.");
        }
    }

    public void showMessage(String message) {
        Ui.showSuccess(statusLabel, message);
    }

    @FXML
    public void addCourse() {
        Navigator.go("AddCourse.fxml");
    }
}

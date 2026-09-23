package org.example.finaloop.controller;

import org.example.finaloop.database.DataBaseStart;
import org.example.finaloop.model.Admin;
import org.example.finaloop.model.Course;
import org.example.finaloop.model.Trainer;
import org.example.finaloop.service.ImageStore;
import org.example.finaloop.service.Session;
import org.example.finaloop.ui.CourseCover;
import org.example.finaloop.ui.Navigator;
import org.example.finaloop.ui.Ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;

public class AddCourseCtrl {

    private static final int MAX_NAME_LENGTH = 150;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;
    private static final double MAX_PRICE = 1000000;
    private static final double PREVIEW_WIDTH = 300;
    private static final double PREVIEW_HEIGHT = 169;

    @FXML private Label titleLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label statusLabel;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private Label descriptionHint;
    @FXML private TextField priceField;
    @FXML private ComboBox<Trainer> trainerCombo;
    @FXML private Label nameError;
    @FXML private Label priceError;
    @FXML private Label trainerError;
    @FXML private Label noTrainersHint;
    @FXML private StackPane coverPreviewBox;
    @FXML private Button removeImageButton;
    @FXML private Button saveButton;

    private Course editingCourse;
    private String currentImagePath;
    private File newImageFile;

    @FXML
    public void initialize() {
        trainerCombo.getItems().addAll(DataBaseStart.getAllTrainers());
        boolean noTrainers = trainerCombo.getItems().isEmpty();
        noTrainersHint.setVisible(noTrainers);
        noTrainersHint.setManaged(noTrainers);

        nameField.textProperty().addListener((obs, oldValue, newValue) -> updatePreview());
        descriptionArea.textProperty().addListener((obs, oldValue, newValue) ->
                descriptionHint.setText(newValue.length() + " / " + MAX_DESCRIPTION_LENGTH));

        updatePreview();
    }

    public void setCourseForEdit(Course course) {
        this.editingCourse = course;
        titleLabel.setText("Edit Course");
        subtitleLabel.setText("Change the course details and press Save.");
        saveButton.setText("Save");

        nameField.setText(course.getName());
        descriptionArea.setText(course.getDescription() == null ? "" : course.getDescription());
        priceField.setText(String.valueOf(course.getPrice()));
        currentImagePath = course.getImagePath();

        for (Trainer trainer : trainerCombo.getItems()) {
            if (trainer.getUserId() == course.getTrainerId()) {
                trainerCombo.setValue(trainer);
            }
        }
        updatePreview();
    }

    @FXML
    public void chooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose a cover image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        File file = chooser.showOpenDialog(Navigator.getStage());
        if (file != null) {
            newImageFile = file;
            updatePreview();
        }
    }

    @FXML
    public void removeImage() {
        newImageFile = null;
        currentImagePath = null;
        updatePreview();
    }

    private void updatePreview() {
        String path = (newImageFile != null) ? newImageFile.getPath() : currentImagePath;
        int id = (editingCourse != null) ? editingCourse.getId() : 0;
        String name = nameField.getText().isBlank() ? "New course" : nameField.getText();

        coverPreviewBox.getChildren().setAll(
                CourseCover.build(id, name, path, PREVIEW_WIDTH, PREVIEW_HEIGHT, CourseCover.PREVIEW));

        boolean hasImage = path != null;
        removeImageButton.setVisible(hasImage);
        removeImageButton.setManaged(hasImage);
    }

    @FXML
    public void saveCourse() {
        clearErrors();
        Ui.hide(statusLabel);

        String name = nameField.getText().trim();
        String description = descriptionArea.getText() == null ? "" : descriptionArea.getText().trim();
        String priceText = priceField.getText().trim();
        Trainer trainer = trainerCombo.getValue();

        boolean valid = true;
        if (name.isEmpty()) {
            showFieldError(nameError, nameField, "Please enter the course name!");
            valid = false;
        } else if (name.length() > MAX_NAME_LENGTH) {
            showFieldError(nameError, nameField, "Course name is too long! (max " + MAX_NAME_LENGTH + " chars)");
            valid = false;
        }

        double price = 0;
        if (priceText.isEmpty()) {
            showFieldError(priceError, priceField, "Please enter the price!");
            valid = false;
        } else {
            try {
                price = Double.parseDouble(priceText);
                if (price < 0) {
                    showFieldError(priceError, priceField, "Price can't be negative!");
                    valid = false;
                } else if (price > MAX_PRICE) {
                    showFieldError(priceError, priceField, "Price is too big!");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                showFieldError(priceError, priceField, "Price must be a number! Example: 199.99");
                valid = false;
            }
        }

        if (trainer == null) {
            showFieldError(trainerError, trainerCombo, "Please choose a trainer!");
            valid = false;
        }

        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            Ui.showError(statusLabel, "Description is too long! (max " + MAX_DESCRIPTION_LENGTH + " chars)");
            valid = false;
        }

        if (!valid) {
            return;
        }

        Admin admin = new Admin(Session.getCurrentUserId(),
                DataBaseStart.getUserNameById(Session.getCurrentUserId()), Session.getCurrentEmail());
        if (!admin.isAuthorizedAdmin()) {
            Ui.showError(statusLabel, "Access denied: only an admin can do this!");
            return;
        }

        String imagePath = currentImagePath;
        if (newImageFile != null) {
            imagePath = ImageStore.saveCourseImage(newImageFile);
            if (imagePath == null) {
                Ui.showError(statusLabel, "Something went wrong while saving the image :(");
                return;
            }
        }

        if (editingCourse == null) {
            boolean success = admin.addCourse(name, description, price, trainer.getUserId(), imagePath);
            if (success) {
                clearFields();
                Ui.showSuccess(statusLabel, "Course \"" + name + "\" added successfully!");
            } else {
                Ui.showError(statusLabel, "Something went wrong while adding the course :(");
            }
        } else {
            boolean success = admin.updateCourse(editingCourse.getId(), name, description, price,
                    trainer.getUserId(), imagePath);
            if (success) {
                CoursesListCtrl list = Navigator.go("CoursesList.fxml");
                if (list != null) {
                    list.showMessage("Course \"" + name + "\" updated successfully!");
                }
            } else {
                Ui.showError(statusLabel, "Something went wrong while saving the changes :(");
            }
        }
    }

    private void showFieldError(Label errorLabel, Control field, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        field.getStyleClass().add("input-error");
    }

    private void clearErrors() {
        for (Label label : new Label[]{nameError, priceError, trainerError}) {
            label.setVisible(false);
            label.setManaged(false);
        }
        nameField.getStyleClass().remove("input-error");
        priceField.getStyleClass().remove("input-error");
        trainerCombo.getStyleClass().remove("input-error");
    }

    private void clearFields() {
        nameField.clear();
        descriptionArea.clear();
        priceField.clear();
        trainerCombo.setValue(null);
        newImageFile = null;
        currentImagePath = null;
        updatePreview();
    }

    @FXML
    public void back() {
        Navigator.go(editingCourse == null ? "AdminDashboard.fxml" : "CoursesList.fxml");
    }
}

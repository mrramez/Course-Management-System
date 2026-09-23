package org.example.finaloop.controller;

import org.example.finaloop.database.DataBaseStart;
import org.example.finaloop.ui.Navigator;
import org.example.finaloop.ui.Ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ReviewPopupCtrl {

    @FXML private Label courseNameLabel;
    @FXML private HBox starsBox;
    @FXML private Label ratingText;
    @FXML private TextArea commentArea;
    @FXML private Label statusLabel;

    private int studentId;
    private int courseId;
    private int rating = 5;

    @FXML
    public void initialize() {
        for (int i = 1; i <= 5; i++) {
            int value = i;
            Button star = new Button();
            star.setGraphic(Ui.icon("star"));
            star.getStyleClass().add("star-button");
            star.setOnAction(e -> setRating(value));
            starsBox.getChildren().add(star);
        }
        setRating(5);
    }

    private void setRating(int value) {
        rating = value;
        for (int i = 0; i < starsBox.getChildren().size(); i++) {
            starsBox.getChildren().get(i).getStyleClass().remove("star-on");
            if (i < value) {
                starsBox.getChildren().get(i).getStyleClass().add("star-on");
            }
        }
        ratingText.setText("Rating: " + value + " / 5");
    }

    private void setCourse(int studentId, int courseId, String courseName) {
        this.studentId = studentId;
        this.courseId = courseId;
        courseNameLabel.setText("Review: " + courseName);
    }

    @FXML
    public void submitReview(ActionEvent event) {
        String comment = commentArea.getText().trim();

        if (rating < 1 || rating > 5) {
            Ui.showError(statusLabel, "Please choose a rating from 1 to 5.");
            return;
        }

        boolean success = DataBaseStart.addReview(studentId, courseId, rating, comment);
        if (success) {
            Ui.showSuccess(statusLabel, "Your review has been saved, thank you!");
            closeWindow(event);
        } else {
            Ui.showError(statusLabel, "Something went wrong while saving your review.");
        }
    }

    private void closeWindow(ActionEvent event) {
        Button source = (Button) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void cancel(ActionEvent event) {
        Button source = (Button) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    public static void open(int studentId, int courseId, String courseName) {
        try {
            FXMLLoader loader = new FXMLLoader(ReviewPopupCtrl.class.getResource("/org/example/finaloop/view/ReviewPopup.fxml"));
            Parent root = loader.load();
            Navigator.applyTheme(root);

            ReviewPopupCtrl controller = loader.getController();
            controller.setCourse(studentId, courseId, courseName);

            Stage popupStage = new Stage();
            popupStage.setTitle("Review Course");
            popupStage.initOwner(Navigator.getStage());
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setResizable(false);
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

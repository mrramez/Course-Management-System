package org.example.finaloop.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class Ui {

    private Ui() {
    }

    public static void showSuccess(Label banner, String text) {
        showBanner(banner, text, "banner-success");
    }

    public static void showError(Label banner, String text) {
        showBanner(banner, text, "banner-error");
    }

    public static void showInfo(Label banner, String text) {
        showBanner(banner, text, "banner-info");
    }

    public static void hide(Label banner) {
        banner.setText("");
        banner.setVisible(false);
        banner.setManaged(false);
    }

    private static void showBanner(Label banner, String text, String kind) {
        banner.getStyleClass().removeAll("banner-success", "banner-error", "banner-info");
        if (!banner.getStyleClass().contains("banner")) {
            banner.getStyleClass().add("banner");
        }
        banner.getStyleClass().add(kind);
        banner.setText(text);
        banner.setWrapText(true);
        banner.setVisible(true);
        banner.setManaged(true);
    }

    public static Label badge(String text, String kind) {
        Label label = new Label(text);
        label.getStyleClass().addAll("badge", "badge-" + kind);
        return label;
    }

    public static Region icon(String name) {
        Region region = new Region();
        region.getStyleClass().add("icon");
        region.setId("icon-" + name);
        return region;
    }

    public static String initials(String name) {
        if (name == null || name.isBlank()) {
            return "?";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
    }

    public static String money(double price) {
        if (price == Math.floor(price)) {
            return String.format("%.0f SAR", price);
        }
        return String.format("%.2f SAR", price);
    }

    public static String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    public static VBox emptyState(String title, String text, Button action) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("empty-title");

        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("empty-text");
        textLabel.setWrapText(true);

        VBox box = new VBox(8, titleLabel, textLabel);
        box.getStyleClass().add("empty-state");
        box.setMaxWidth(Double.MAX_VALUE);
        if (action != null) {
            VBox.setMargin(action, new Insets(8, 0, 0, 0));
            box.getChildren().add(action);
        }
        return box;
    }

    public static void setupThemeButton(Button button) {
        updateThemeButton(button);
        button.setOnAction(e -> {
            Navigator.toggleDarkMode();
            updateThemeButton(button);
        });
    }

    private static void updateThemeButton(Button button) {
        boolean dark = Navigator.isDarkMode();
        button.setText(dark ? "Light mode" : "Dark mode");
        button.setGraphic(icon(dark ? "sun" : "moon"));
    }

    public static boolean confirm(String title, String message, String confirmText, boolean danger) {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.initOwner(Navigator.getStage());
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);

        ButtonType confirmButton = new ButtonType(confirmText, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(cancelButton, confirmButton);

        DialogPane pane = alert.getDialogPane();
        pane.getStylesheets().add(Navigator.getThemeUrl());
        Navigator.applyTheme(pane);
        Button confirmNode = (Button) pane.lookupButton(confirmButton);
        confirmNode.getStyleClass().add(danger ? "btn-danger" : "btn-primary");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == confirmButton;
    }
}

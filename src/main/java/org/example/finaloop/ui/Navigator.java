package org.example.finaloop.ui;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.prefs.Preferences;

public class Navigator {

    private static final String THEME = Navigator.class.getResource("/org/example/finaloop/styles/theme.css").toExternalForm();
    private static final Preferences PREFS = Preferences.userNodeForPackage(Navigator.class);

    private static Stage stage;
    private static Scene scene;
    private static String activePage = "";
    private static boolean darkMode = PREFS.getBoolean("darkMode", false);

    private Navigator() {
    }

    public static void init(Stage mainStage) {
        stage = mainStage;
    }

    public static <T> T go(String fxmlFile) {
        return go(fxmlFile, fxmlFile);
    }

    public static <T> T go(String fxmlFile, String pageName) {
        activePage = pageName;
        try {
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource("/org/example/finaloop/view/" + fxmlFile));
            Parent root = loader.load();
            applyTheme(root);

            if (scene == null) {
                scene = new Scene(root, 1180, 740);
                scene.getStylesheets().add(THEME);
                stage.setScene(scene);
            } else {
                scene.setRoot(root);
            }

            playEnterAnimation(root);
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getActivePage() {
        return activePage;
    }

    public static Stage getStage() {
        return stage;
    }

    public static String getThemeUrl() {
        return THEME;
    }

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void toggleDarkMode() {
        darkMode = !darkMode;
        PREFS.putBoolean("darkMode", darkMode);
        if (scene != null) {
            applyTheme(scene.getRoot());
        }
    }

    public static void applyTheme(Parent root) {
        root.getStyleClass().remove("dark");
        if (darkMode) {
            root.getStyleClass().add("dark");
        }
    }

    private static void playEnterAnimation(Node node) {
        FadeTransition fade = new FadeTransition(Duration.millis(220), node);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(220), node);
        slide.setFromY(8);
        slide.setToY(0);

        new ParallelTransition(fade, slide).play();
    }
}

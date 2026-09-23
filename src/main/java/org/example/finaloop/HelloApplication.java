package org.example.finaloop;

import org.example.finaloop.database.DataBaseStart;
import org.example.finaloop.ui.Navigator;

import javafx.application.Application;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {

        DataBaseStart.initializeDataBase();

        Navigator.init(stage);
        stage.setTitle("Courses Management");
        stage.setMinWidth(1000);
        stage.setMinHeight(660);
        Navigator.go("Welcoming.fxml");
        stage.show();
    }

}

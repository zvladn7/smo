package com.github.zvladn7.graphics;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class GraphMain extends Application {

    private static final Logger logger = LoggerFactory.getLogger(GraphMain.class);
    private static String MAIN_WINDOW_FILE_PATH
            = "src/main/java/com/github/zvladn7/graphics/fxmlfiles/MainWindow.fxml";
    public static String FXML_FILE_PARENT_PATH = "src/main/java/com/github/zvladn7/graphics/fxmlfiles/";
//    public static String MAIN_WINDOW_FILE_PATH = FXML_FILE_PARENT_PATH + "MainWindow.fxml";




    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            Parent root = FXMLLoader.load(new File(MAIN_WINDOW_FILE_PATH).toURI().toURL());
            stage.setTitle("Main window");
            stage.setScene(new Scene(root, 1200, 900));
            stage.show();
        } catch (IOException e) {
            logger.error("Cannot start main window", e);
        }
    }
}

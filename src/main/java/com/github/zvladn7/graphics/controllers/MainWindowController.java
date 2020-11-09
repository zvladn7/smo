package com.github.zvladn7.graphics.controllers;

import com.github.zvladn7.graphics.GraphMain;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class MainWindowController {

    private static final Logger logger = LoggerFactory.getLogger(GraphMain.class);


    @FXML
    private Button autoModeBtn;

    @FXML
    private Button stepModeBtn;

    @FXML
    private Button setSettingsBtn;

    @FXML
    private Button exitBtn;


    @FXML
    void initialize() {
        setOnActionForButton(autoModeBtn, getPathByFileName("AutoModeWindow.fxml"));
        setOnActionForButton(stepModeBtn, getPathByFileName("StepModeWindow.fxml"));
        setOnActionForButton(setSettingsBtn, getPathByFileName("SetSettingsWindow.fxml"));
    }

    private static String getPathByFileName(final String fileName) {
        return GraphMain.FXML_FILE_PARENT_PATH + fileName;
    }

    private static void setOnActionForButton(Button button, String path) {
        button.setOnAction(actionEvent -> {

            FXMLLoader loader = new FXMLLoader();
            try {
                loader.setLocation(new File(path).toURI().toURL());
                loader.load();
            } catch (IOException e) {
                logger.error("Cannot load fxml file", e);
            }

            final Parent root = loader.getRoot();
            final Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.showAndWait();
        });
    }

    @FXML
    void exitClick() {
        Stage stage = (Stage) exitBtn.getScene().getWindow();
        stage.close();
    }

}

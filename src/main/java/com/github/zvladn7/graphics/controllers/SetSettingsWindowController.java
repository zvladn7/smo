package com.github.zvladn7.graphics.controllers;

import com.github.zvladn7.Controller;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetSettingsWindowController {

    private static final Logger logger = LoggerFactory.getLogger(SetSettingsWindowController.class);

    public static Controller controller = Controller.newBuilder()
            .setAlpha(1)
            .setBeta(3)
            .setAmountOfDevices(4)
            .setAmountOfSources(4)
            .setBufferSize(10)
            .setRequestsNumber(1000)
            .build();

    @FXML
    private TextField alphaValue;

    @FXML
    private TextField betaValue;

    @FXML
    private TextField sourcesNumValue;

    @FXML
    private TextField devicesNumValue;

    @FXML
    private TextField bufSizeValue;

    @FXML
    private TextField requestNumValue;

    @FXML
    void setBtnClick() {
        final String alpha = alphaValue.getText();
        final String beta = betaValue.getText();
        final String sourcesNum = sourcesNumValue.getText();
        final String devicesNum = devicesNumValue.getText();
        final String bufSize = bufSizeValue.getText();
        final String requestsNum = requestNumValue.getText();

        try {
            controller = Controller.newBuilder()
                    .setAlpha(validateIntAndGet(alpha))
                    .setBeta(validateIntAndGet(beta))
                    .setAmountOfDevices(validateIntAndGet(devicesNum))
                    .setAmountOfSources(validateIntAndGet(sourcesNum))
                    .setBufferSize(validateIntAndGet(bufSize))
                    .setRequestsNumber(validateIntAndGet(requestsNum))
                    .build();
        } catch (IllegalArgumentException ex) {
            logger.error("Невозможно установить параметры", ex);
        }
    }

    private int validateIntAndGet(final String num) {
        try {
            return Integer.parseInt(num);
        } catch (NumberFormatException ex) {
            showAlert("Невозможно преобразование",
                    "Неверный формат ввода.\nВсе параметры - целые числа",
                    Alert.AlertType.ERROR);
            throw new IllegalArgumentException(ex);
        }
    }

    private void showAlert(String header, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(header);
        Text text = new Text(content);
        text.setWrappingWidth(350);
        alert.getDialogPane().setContent(text);
        alert.showAndWait();
    }

}

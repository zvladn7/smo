package com.github.zvladn7.graphics.controllers;

import com.github.zvladn7.Controller;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetSettingsWindowController {

    private static final Logger logger = LoggerFactory.getLogger(SetSettingsWindowController.class);

    public static Controller controller = Controller.newBuilder()
            .setAlpha(1)
            .setBeta(3)
            .setLamda(1)
            .setAmountOfDevices(4)
            .setAmountOfSources(4)
            .setBufferSize(10)
            .setRequestsNumber(1000)
            .build();

    @FXML
    private Button setBtn;

    @FXML
    private TextField alphaValue;

    @FXML
    private TextField betaValue;

    @FXML
    private TextField sourcesNumValue;

    @FXML
    private TextField lamdaValue;

    @FXML
    private TextField devicesNumValue;

    @FXML
    private TextField bufSizeValue;

    @FXML
    private TextField requestNumValue;

    @FXML
    void initialize() {
        alphaValue.setText(String.valueOf(controller.getAlpha()));
        betaValue.setText(String.valueOf(controller.getBeta()));
        sourcesNumValue.setText(String.valueOf(controller.getAmountOfSources()));
        lamdaValue.setText(String.valueOf(controller.getLamda()));
        devicesNumValue.setText(String.valueOf(controller.getAmountOfDevices()));
        bufSizeValue.setText(String.valueOf(controller.getBufferSize()));
        requestNumValue.setText(String.valueOf(controller.getRequestsNumber()));
    }

    @FXML
    void setBtnClick() {
        final String alpha = alphaValue.getText();
        final String beta = betaValue.getText();
        final String sourcesNum = sourcesNumValue.getText();
        final String lamda = lamdaValue.getText();
        final String devicesNum = devicesNumValue.getText();
        final String bufSize = bufSizeValue.getText();
        final String requestsNum = requestNumValue.getText();

        try {
            controller = Controller.newBuilder()
                    .setAlpha(validateDoubleAndGet(alpha))
                    .setBeta(validateDoubleAndGet(beta))
                    .setLamda(validateDoubleAndGet(lamda))
                    .setAmountOfDevices(validateIntAndGet(devicesNum))
                    .setAmountOfSources(validateIntAndGet(sourcesNum))
                    .setBufferSize(validateIntAndGet(bufSize))
                    .setRequestsNumber(validateIntAndGet(requestsNum))
                    .build();
        } catch (IllegalArgumentException ex) {
            logger.error("Невозможно установить параметры", ex);
        }

        showAlert("Усатновка параметров",
                "Параметры успешно установлены!",
                Alert.AlertType.INFORMATION);
        Stage stage = (Stage) setBtn.getScene().getWindow();
        stage.close();
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

    private double validateDoubleAndGet(final String num) {
        try {
            return Double.parseDouble(num);
        } catch (NumberFormatException ex) {
            showAlert("Невозможно преобразование",
                    "Неверный формат ввода.\nВсе параметры - целые числа",
                    Alert.AlertType.ERROR);
            throw new IllegalArgumentException(ex);
        }
    }

    private void showAlert(final String header,
                           final String content,
                           final Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(header);
        Text text = new Text(content);
        text.setWrappingWidth(350);
        alert.getDialogPane().setContent(text);
        alert.showAndWait();
    }

}

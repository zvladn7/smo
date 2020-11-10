package com.github.zvladn7.graphics.controllers;

import com.github.zvladn7.Controller;
import com.github.zvladn7.analytics.Analytics;
import com.github.zvladn7.analytics.DeviceResults;
import com.github.zvladn7.analytics.SourceResults;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;
import java.util.function.Supplier;

public class AutoModeWindowController {

    @FXML
    private TableView<SourceResults> sourceTable;

    @FXML
    private TableView<DeviceResults> deviceTable;


    @FXML
    void initialize() {
        sourceTable.getItems().clear();
        deviceTable.getItems().clear();
        final Analytics analytics = SetSettingsWindowController.controller.modulateWork();
        setTableData(sourceTable, analytics::getSourceResultsList);
        setTableData(deviceTable, analytics::getDeviceResultsList);
    }

    private static <T> void setTableData(final TableView<T> table, final Supplier<List<T>> result) {
        final List<T> deviceResults = result.get();
        for (final T res : deviceResults) {
            table.getItems().add(res);
        }
    }

}


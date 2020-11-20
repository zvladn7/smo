package com.github.zvladn7;

import com.github.zvladn7.controllers.Controller;
import com.github.zvladn7.controllers.StepModeController;

public class Main {

    public static void main(String[] args) {
        Controller controller = Controller.newBuilder()
                .setAlpha(1)
                .setBeta(3)
                .setLamda(1.2)
                .setAmountOfDevices(4)
                .setAmountOfSources(4)
                .setBufferSize(5)
                .setRequestsNumber(1000)
                .build();

//        controller.autoMode();
        StepModeController stepModeController = new StepModeController(controller);
        stepModeController.stepMode();
    }
}

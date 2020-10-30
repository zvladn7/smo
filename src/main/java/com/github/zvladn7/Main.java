package com.github.zvladn7;

public class Main {

    public static void main(String[] args) {
        Controller controller = Controller.newBuilder()
                .setAlpha(0.2)
                .setBeta(1)
                .setAmountOfDevices(4)
                .setAmountOfSources(4)
                .setBufferSize(10)
                .setRequestsNumber(1000)
                .build();

        controller.modulateWork(new Analytics());
    }
}

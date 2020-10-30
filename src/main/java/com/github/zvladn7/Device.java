package com.github.zvladn7;

import java.util.Random;

public class Device {

    private final Random generator = new Random();
    private static int counter = 0;

    private final int number;

    private Request requestOnDevice;
    private double timeToDone;
    public Device(int number) {
        this.number = number;
        this.timeToDone = -1;
        this.requestOnDevice = null;
    }

    public int getNumber() {
        return number;
    }

    public boolean isFree() {
        return requestOnDevice == null;
    }

    public void execute(final Request request, final double currentTime) {
        final double timeToDo = getTimeOnDevice();
        this.requestOnDevice = request;
        this.timeToDone = currentTime + timeToDo;
        System.out.println("Execute " + counter++);
    }

    public void clearAfterDoneProcessing() {
        this.requestOnDevice = null;
        this.timeToDone = -1;
    }


    public Request getDoneRequest() {
        return requestOnDevice;
    }

    public double getDoneTime() {
        return timeToDone;
    }

    private double getTimeOnDevice() {
        return Math.exp(generator.nextDouble());
    }
}

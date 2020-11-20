package com.github.zvladn7.components;

import java.util.Random;

public class Device {

    private final Random generator = new Random();
    private static int counter = 0;

    private final int number;
    private final double lamda;

    private Request requestOnDevice;
    private double timeToDone;
    private double startTime;

    public Device(final int number, final double lamda) {
        this.number = number;
        this.lamda = lamda;
        this.startTime = -1;
        this.timeToDone = 0;
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
        System.out.println("Time on device: " + timeToDo);
        this.requestOnDevice = request;
        this.startTime = currentTime;
        this.timeToDone = currentTime + timeToDo;
        System.out.println("Execute " + counter++);
    }

    public void clearAfterDoneProcessing() {
        this.requestOnDevice = null;
        this.startTime = -1;
    }

    public Request getDoneRequest() {
        return requestOnDevice;
    }

    public double getStartTime() {
        return startTime;
    }

    public double getDoneTime() {
        return timeToDone;
    }

    private double getTimeOnDevice() {
        return lamda * Math.exp(generator.nextDouble());
    }
}

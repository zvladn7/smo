package com.github.zvladn7.analytics;

public class DeviceResults {

    private final int deviceNumber;
    private final double timeOfWork;
    private final double timeOfRealization;
    private final double coefOfRealization;

    public DeviceResults(int deviceNumber, double timeOfWork, double timeOfRealization, double coefOfRealization) {
        this.deviceNumber = deviceNumber;
        this.timeOfWork = timeOfWork;
        this.timeOfRealization = timeOfRealization;
        this.coefOfRealization = coefOfRealization;
    }

    public int getDeviceNumber() {
        return deviceNumber;
    }

    public double getTimeOfWork() {
        return timeOfWork;
    }

    public double getTimeOfRealization() {
        return timeOfRealization;
    }

    public double getCoefOfRealization() {
        return coefOfRealization;
    }
}

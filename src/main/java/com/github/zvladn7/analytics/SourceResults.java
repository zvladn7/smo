package com.github.zvladn7.analytics;

public class SourceResults {

    private final int sourceNumber;
    private final int genNumber;
    private final int processedNumber;
    private final int canceledNumber;
    private final double probCancel;
    private final double timeInSystem;
    private final double timeOfWait;
    private final double timeOnDevice;
    private final double dispWait;
    private final double dispProc;

    public SourceResults(int sourceNumber, int genNumber, int processedNumber, int canceledNumber, double probCancel, double timeInSystem, double timeOfWait, double timeOnDevice, double dispWait, double dispProc) {
        this.sourceNumber = sourceNumber;
        this.genNumber = genNumber;
        this.processedNumber = processedNumber;
        this.canceledNumber = canceledNumber;
        this.probCancel = probCancel;
        this.timeInSystem = timeInSystem;
        this.timeOfWait = timeOfWait;
        this.timeOnDevice = timeOnDevice;
        this.dispWait = dispWait;
        this.dispProc = dispProc;
    }

    public int getSourceNumber() {
        return sourceNumber;
    }

    public int getGenNumber() {
        return genNumber;
    }

    public int getProcessedNumber() {
        return processedNumber;
    }

    public int getCanceledNumber() {
        return canceledNumber;
    }

    public double getProbCancel() {
        return probCancel;
    }

    public double getTimeInSystem() {
        return timeInSystem;
    }

    public double getTimeOfWait() {
        return timeOfWait;
    }

    public double getTimeOnDevice() {
        return timeOnDevice;
    }

    public double getDispWait() {
        return dispWait;
    }

    public double getDispProc() {
        return dispProc;
    }

    @Override
    public String toString() {
        return "SourceResults{" +
                "sourceNumber=" + sourceNumber +
                ", genNumber=" + genNumber +
                ", processedNumber=" + processedNumber +
                ", canceledNumber=" + canceledNumber +
                ", probCancel=" + probCancel +
                ", timeInSystem=" + timeInSystem +
                ", timeOfWait=" + timeOfWait +
                ", timeOfWork=" + timeOnDevice +
                ", dispWait=" + dispWait +
                ", dispProc=" + dispProc +
                '}';
    }

}

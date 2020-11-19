package com.github.zvladn7.analytics;

import com.github.zvladn7.Pair;
import com.github.zvladn7.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

public class Analytics {


    private static final Logger logger = LoggerFactory.getLogger(Analytics.class);

    //1 - количество сгенирированных заявок каждым источником
    //2 - вероятность отказа в обслуживании заявок каждого источка
    //а - обслуженные, б - нет;
    // вероянтность=б/(а+б)
    /*
    3 - среднее время пребывания заявок каждого источника в системе
    4 - среднее время ожидания заявок каждого источника
    5 - среднее время обслуживания заявок каждого источника
    6 - дисперсия двух последних величин
        в данном случае - это:
            - для 4: время ожидания/время в системе
            - для 5: время обработки/время в системе
    7 - коэфициент использования устройств (время работы каждого прибора / время реализации)
     */


    /**
     * Map field that representing analytics of process.
     * Where:
     * Key: Double is a time of moment when the event was happened
     * Value: Pair of event type and pair number of (sources, buf position or device) depend on type and source
     */
    private TreeMap<Pair<Double, Pair<EventType, Integer>>, Integer> analyticsByStep;
    private double fullTimeOfWork;

    /**
     * Types of special events in the system.
     */
    public enum EventType {
        GENERATED_REQUEST,
        CANCELED_REQUEST,
        PUT_TO_BUFFER,
        FREE_DEVICE,
        REMOVE_FROM_BUFFER,
        PUT_ON_DEVICE
    }

    //for sources
    private int[] amountOfGeneratedRequests;
    private int[] amountOfFailed;
    private int[] amountOfProcessed;
    private double[] timeInSystem;
    private double[] timeOfWait;
    private double[] timeOnDevice;

    //for devices
    private double[] deviceWorkTime;

    public Analytics(final int sourcesAmount, final int devicesAmount) {
        analyticsByStep = new TreeMap<>((first, second) -> {
            int compare = first.getKey().compareTo(second.getKey());
            if (compare > 0) {
                return 1;
            } else if (compare == 0) {
                Pair<EventType, Integer> firstValue = first.getValue();
                Pair<EventType, Integer> secondValue = second.getValue();
                compare = firstValue.getKey().compareTo(secondValue.getKey());
                if (compare == 0) {
                    return firstValue.getValue().compareTo(secondValue.getValue());
                } else {
                    return compare;
                }
            } else {
                return -1;
            }
        });
        statArraysInit(sourcesAmount, devicesAmount);
    }

    private void statArraysInit(final int sourcesAmount, int devicesAmount) {
        amountOfGeneratedRequests = new int[sourcesAmount];
        amountOfFailed = new int[sourcesAmount];
        amountOfProcessed = new int[sourcesAmount];
        timeInSystem = new double[sourcesAmount];
        timeOfWait = new double[sourcesAmount];
        timeOnDevice = new double[sourcesAmount];
        System.out.println(Arrays.toString(timeInSystem));
        System.out.println(Arrays.toString(timeOfWait));
        System.out.println(Arrays.toString(timeOnDevice));
        deviceWorkTime = new double[devicesAmount];
    }

    public void addGeneratedRequest(final Request request) {
        final int sourceNumber = request.getSourceNumber();
        amountOfGeneratedRequests[sourceNumber]++;
        analyticsByStep.put(
                new Pair<>(request.getInitialTime(), new Pair<>(EventType.GENERATED_REQUEST, sourceNumber)),
                sourceNumber);
    }

    public void cancelRequest(final Request request, final Request nextRequest, final int bufferPos) {
        final int sourceNumber = request.getSourceNumber();
        final double initialTime = request.getInitialTime();
        amountOfFailed[sourceNumber]++;
        timeOfWait[sourceNumber] += nextRequest.getInitialTime() - initialTime;
        logger.info("Время ожидания выбитой заявки: {}", (nextRequest.getInitialTime() - initialTime));
        if (bufferPos != -1) {
            analyticsByStep.put(
                    new Pair<>(initialTime, new Pair<>(EventType.CANCELED_REQUEST, nextRequest.getSourceNumber())),
                    bufferPos);
        }
    }

    public void addRequestToBuffer(final Request nextRequest,
                                   final Integer bufferPosition) {
        analyticsByStep.put(
                new Pair<>(nextRequest.getInitialTime(), new Pair<>(EventType.PUT_TO_BUFFER, nextRequest.getSourceNumber())),
                bufferPosition);
    }

    public void removeFromBuffer(final Request request,
                                 final Integer bufferPosition,
                                 final double time) {
        final int sourceNumber = request.getSourceNumber();
        timeOfWait[sourceNumber] += time - request.getInitialTime();
        logger.info("Время ожидания заявки в буфере: {}", (time - request.getInitialTime()));
        analyticsByStep.put(
                new Pair<>(time, new Pair<>(EventType.REMOVE_FROM_BUFFER, request.getSourceNumber())),
                bufferPosition);
    }

    public void putOnDevice(final int deviceNumber,
                            final double time,
                            final int sourceNumber) {
        analyticsByStep.put(
                new Pair<>(time, new Pair<>(EventType.PUT_ON_DEVICE, sourceNumber)),
                deviceNumber);
    }

    public void addDoneRequest(final int deviceNumber,
                               final Request request,
                               final double timeOnDevice,
                               final double timeOfWork) {
        final int sourceNumber = request.getSourceNumber();
        this.timeOnDevice[sourceNumber] += timeOnDevice;
        this.deviceWorkTime[deviceNumber] += timeOfWork;
        this.amountOfProcessed[sourceNumber]++;
        this.analyticsByStep.put(
                new Pair<>(timeOnDevice, new Pair<>(EventType.FREE_DEVICE, request.getSourceNumber())),
                deviceNumber);
    }

    public void calcTimeInSystem() {
        for (int i = 0; i < timeInSystem.length; i++) {
            timeInSystem[i] = timeOfWait[i] + timeOnDevice[i];
        }
    }

    public TreeMap<Pair<Double, Pair<EventType, Integer>>, Integer> getAnalyticsByStep() {
        return analyticsByStep;
    }

    public List<SourceResults> getSourceResultsList() {
        final List<SourceResults> resultsList = new ArrayList<>();
        for (int i = 0; i < amountOfGeneratedRequests.length; ++i) {
            resultsList.add(new SourceResults(
                    i,
                    amountOfGeneratedRequests[i],
                    amountOfProcessed[i],
                    amountOfFailed[i],
                    getCancelProbability(i),
                    getAvgTimeInSystem(i),
                    getAvgTimeOfWait(i),
                    getAvgTimeOnDevice(i),
                    getDispOfWait(i),
                    getDispOfProcess(i)
            ));
        }
        return resultsList;
    }

    public List<DeviceResults> getDeviceResultsList() {
        final List<DeviceResults> deviceResults = new ArrayList<>();
        for (int i = 0; i < deviceWorkTime.length; ++i) {
            deviceResults.add(new DeviceResults(
                    i,
                    deviceWorkTime[i],
                    fullTimeOfWork,
                    getCoefOfRealization(i)
            ));
        }
        return deviceResults;
    }

    private double getCoefOfRealization(final int deviceNumber) {
        return deviceWorkTime[deviceNumber] / fullTimeOfWork;
    }

    public void setFullTimeOfWork(double fullTimeOfWork) {
        this.fullTimeOfWork = fullTimeOfWork;
    }

    private double getAvgTimeInSystem(final int sourceNumber) {
        return timeInSystem[sourceNumber] / amountOfGeneratedRequests[sourceNumber];
    }

    private double getAvgTimeOfWait(final int sourceNumber) {
        return timeOfWait[sourceNumber] / amountOfGeneratedRequests[sourceNumber];
    }

    private double getAvgTimeOnDevice(final int sourceNumber) {
        return timeOnDevice[sourceNumber] / amountOfGeneratedRequests[sourceNumber];
    }

    private double getCancelProbability(final int sourceNumber) {
        return (double) amountOfFailed[sourceNumber] / (amountOfProcessed[sourceNumber] + amountOfFailed[sourceNumber]);
    }

    private double getDispOfWait(final int sourceNumber) {
        return timeOfWait[sourceNumber] / timeInSystem[sourceNumber];
    }

    private double getDispOfProcess(final int sourceNumber) {
        return timeOnDevice[sourceNumber] / timeInSystem[sourceNumber];
    }

    public void printStat() {
        System.out.println("____STAT____");
        System.out.println("Generated: ");
        printArray(amountOfGeneratedRequests);
        System.out.println("Processed: ");
        printArray(amountOfProcessed);
        System.out.println("Failed: ");
        printArray(amountOfFailed);
        System.out.println("Time of wait: ");
        printArray(timeOfWait);
        System.out.println("Time of device work: ");
        printArray(timeOnDevice);
        System.out.println("Time in system: ");
        printArray(timeInSystem);
    }

    private void printArray(final int[] arr) {
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i] + " ");
        }
        System.out.println();
    }

    private void printArray(final double[] arr) {
        for (int i = 0; i < arr.length; i++) {
            System.out.printf("%.3f ", arr[i]);
        }
        System.out.println();
    }
}

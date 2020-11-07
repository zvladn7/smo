package com.github.zvladn7;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;

public class Analytics {

    private static final Logger logger = LoggerFactory.getLogger(Analytics.class);

    //1 - количество сгенирированных заявок каждым источником
    //2 - вероятность отказа в обслуживании заявок каждого источка
    //а - обслуженные, б - нет;
    // вероянтность=а/(а+б)
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
     * Value: Pair of event type and number of (sources, buf position or device) depend on type
     */
    private TreeMap<Double, Pair<EventType, Integer>> analytics;

    /**
     * Types of special events in the system.
     */
    enum EventType {
        GENERATED_REQUEST,
        CANCELED_REQUEST,
        PUT_TO_BUFFER,
        REMOVE_FROM_BUFFER,
        PUT_ON_DEVICE,
        FREE_DEVICE
    }

    private int[] amountOfGeneratedRequests;
    private int[] amountOfFailed;
    private int[] amountOfProcessed;
    private double[] timeInSystem;
    private double[] timeOfWait;
    private double[] timeOfDeviceWork;

    public Analytics(final int sourcesAmount) {
        analytics = new TreeMap<>();
        statArraysInit(sourcesAmount);
    }

    private void statArraysInit(final int sourcesAmount) {
        amountOfGeneratedRequests = new int[sourcesAmount];
        amountOfFailed = new int[sourcesAmount];
        amountOfProcessed = new int[sourcesAmount];
        timeInSystem = new double[sourcesAmount];
        timeOfWait = new double[sourcesAmount];
        timeOfDeviceWork = new double[sourcesAmount];
    }

    public void addGeneratedRequest(final Request request) {
        final int sourceNumber = request.getSourceNumber();
        amountOfGeneratedRequests[sourceNumber]++;
        analytics.put(request.getInitialTime(), new Pair<>(EventType.GENERATED_REQUEST, sourceNumber));
    }

    public void cancelRequest(final Request request, final double currentTime) {
        final int sourceNumber = request.getSourceNumber();
        final double initialTime = request.getInitialTime();
        amountOfFailed[sourceNumber]++;
        timeOfWait[sourceNumber] += currentTime - initialTime;
        logger.info("Время ожидания выбитой заявки: {}", (currentTime - initialTime));
        analytics.put(initialTime, new Pair<>(EventType.CANCELED_REQUEST, sourceNumber));
    }

    public void addRequestToBuffer(final Request nextRequest,
                                   final Integer bufferPosition) {
        analytics.put(nextRequest.getInitialTime(), new Pair<>(EventType.PUT_TO_BUFFER, bufferPosition));
    }

    public void removeFromBuffer(final Request request,
                                 final Integer bufferPosition,
                                 final double time) {
        final int sourceNumber = request.getSourceNumber();
        timeOfWait[sourceNumber] += time - request.getInitialTime();
        analytics.put(time, new Pair<>(EventType.REMOVE_FROM_BUFFER, bufferPosition));
    }

    public void putOnDevice(final int deviceNumber,
                            final double time) {
        analytics.put(time, new Pair<>(EventType.PUT_ON_DEVICE, deviceNumber));
    }

    public void addDoneRequest(final int deviceNumber,
                               final Request request,
                               final double timeOnDevice,
                               final double time) {
        final int sourceNumber = request.getSourceNumber();
        timeOfDeviceWork[sourceNumber] += timeOnDevice;
        amountOfProcessed[sourceNumber]++;
        analytics.put(time, new Pair<>(EventType.FREE_DEVICE, deviceNumber));
    }

    public void calcTimeInSystem() {
        for (int i = 0; i < timeInSystem.length; i++) {
            timeInSystem[i] = timeOfWait[i] + timeOfDeviceWork[i];
        }
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
        printArray(timeOfDeviceWork);
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

package com.github.zvladn7.controllers;

import com.github.zvladn7.util.Pair;
import com.github.zvladn7.analytics.Analytics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public final class StepModeController {

    private static final int NOT_BUSY_FLAG_VALUE = -1;
    private static final Logger logger = LoggerFactory.getLogger(StepModeController.class);

    private final Analytics analytics;
    private final int bufferSize;
    private final int amountOfDevices;

    public StepModeController(final Controller controller) {
        this.analytics = controller.modulateWork();
        this.bufferSize = controller.getBufferSize();
        this.amountOfDevices = controller.getAmountOfDevices();
    }

    public void stepMode() {
        final TreeMap<Pair<Double, Pair<Analytics.EventType, Integer>>, Integer> analyticsByStep
                = analytics.getAnalyticsByStep();
        //index - number of slot
        //value - number of source which generate the request
        final int[] bufIsBusy = new int[bufferSize];
        Arrays.fill(bufIsBusy, NOT_BUSY_FLAG_VALUE);
        final int[] deviceIsBusy = new int[amountOfDevices];
        Arrays.fill(deviceIsBusy, NOT_BUSY_FLAG_VALUE);
        final Scanner scanner = new Scanner(System.in);
        int amountInBuf = 0;
        String what = "";
        for (final Map.Entry<Pair<Double, Pair<Analytics.EventType, Integer>>, Integer> entry : analyticsByStep.entrySet()) {
            final int value = entry.getValue();
            logger.info("Следующее особое событие в {}", entry.getKey().key);
            final Analytics.EventType eventType = entry.getKey().value.key;
            final int sourceNum = entry.getKey().value.value;
            switch (eventType) {
                case GENERATED_REQUEST:
                    logger.info("Uсточник {} сгенерировал новую заявку", sourceNum);
                    break;
                case CANCELED_REQUEST:
                    logger.info("Заявка источника {} отменена, индекс буфера {}", sourceNum, value);
                    removeFromBuf(bufIsBusy, value, amountInBuf);
                    amountInBuf--;
                    putToBufEnd(bufIsBusy, sourceNum, amountInBuf);
                    amountInBuf++;
                    break;
                case PUT_TO_BUFFER:
                    logger.info("Заявка источника {} помещена в буфер по индексу {}", sourceNum, value);
                    putToBufEnd(bufIsBusy, sourceNum, amountInBuf);
                    amountInBuf++;
                    break;
                case REMOVE_FROM_BUFFER:
                    logger.info("Заявка источника {} удалена из буфера по индексу {}", sourceNum, value);
                    removeFromBuf(bufIsBusy, value, amountInBuf);
                    amountInBuf--;
                    break;
                case PUT_ON_DEVICE:
                    logger.info("Заявка источника {} поставлена на прибор", sourceNum);
                    putOnDevice(deviceIsBusy, value, sourceNum);
                    break;
                case FREE_DEVICE:
                    logger.info("Заявка источника {} закончила исполнение на приборе", sourceNum);
                    freeDevice(deviceIsBusy, value);
                    break;
            }
            printCurrentStepModeSituation(bufIsBusy, deviceIsBusy);
            logger.info("Нажмите enter, чтобы продолжить...");
            if (!"continue".equals(what)) {
                what = scanner.nextLine();
            }
        }
        analytics.printStat();
    }

    private void putOnDevice(final int[] deviceIsBusy,
                             final int deviceNum,
                             final int sourceNum) {
        deviceIsBusy[deviceNum] = sourceNum;
    }

    private void freeDevice(final int[] deviceIsBusy,
                            final int deviceNum) {
        deviceIsBusy[deviceNum] = -1;
    }

    private void removeFromBuf(final int[] bufIsBusy,
                               final int removedBufPos,
                               final int lastPos) {
        logger.info("Remove from buf with removedBufPos={}, lastPos={}", removedBufPos, lastPos);
        System.arraycopy(bufIsBusy, removedBufPos + 1, bufIsBusy, removedBufPos, bufferSize - removedBufPos - 1);
        bufIsBusy[lastPos - 1] = -1;
    }

    private void putToBufEnd(final int[] bufIsBusy,
                             final int sourceNum,
                             final int amountInBuf) {
        bufIsBusy[amountInBuf] = sourceNum;
    }

    private void printCurrentStepModeSituation(final int[] bufIsBusy,
                                               final int[] deviceIsBusy) {
        for (int i = 0; i < bufIsBusy.length; ++i) {
            logger.info("Буфер, позиция={} : источник={}", i, bufIsBusy[i] == -1 ? "null" : bufIsBusy[i]);
        }
        for (int i = 0; i < deviceIsBusy.length; ++i) {
            logger.info("Прибор, номер={} : источник={}", i, deviceIsBusy[i] == -1 ? "null" : deviceIsBusy[i]);
        }
    }

}

package com.github.zvladn7;

import com.github.zvladn7.SelectionManager.DoneInfo;
import com.github.zvladn7.analytics.Analytics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Controller {

    private static final int NOT_BUSY_FLAG_VALUE = -1;
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    private double alpha;
    private double beta;
    private int amountOfSources;
    private double lamda;
    private int amountOfDevices;
    private int bufferSize;
    private int requestsNumber;

    private Controller() {

    }

    public double getAlpha() {
        return alpha;
    }

    public double getBeta() {
        return beta;
    }

    public int getAmountOfSources() {
        return amountOfSources;
    }

    public double getLamda() {
        return lamda;
    }

    public int getAmountOfDevices() {
        return amountOfDevices;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getRequestsNumber() {
        return requestsNumber;
    }

    public class Builder {

        private Builder() {

        }

        public Builder setAlpha(final double alpha) {
            Controller.this.alpha = alpha;
            return this;
        }

        public Builder setBeta(final double beta) {
            Controller.this.beta = beta;
            return this;
        }

        public Builder setAmountOfSources(final int amount) {
            Controller.this.amountOfSources = amount;
            return this;
        }

        public Builder setLamda(final double lamda) {
            Controller.this.lamda = lamda;
            return this;
        }

        public Builder setAmountOfDevices(final int amount) {
            Controller.this.amountOfDevices = amount;
            return this;
        }

        public Builder setBufferSize(final int size) {
            Controller.this.bufferSize = size;
            return this;
        }

        public Builder setRequestsNumber(final int amount) {
            Controller.this.requestsNumber = amount;
            return this;
        }

        public Controller build() {
            return Controller.this;
        }

    }

    public static Builder newBuilder() {
        return new Controller().new Builder();
    }


    public void autoMode() {
        final Analytics analytics = modulateWork();
        analytics.printStat();
    }

    public void stepMode() {
        final Analytics analytics = modulateWork();
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
                    logger.info("Источник {} сгенерировал новую заявку", sourceNum);
                    break;
                case CANCELED_REQUEST:
                    logger.info("Заявка источника {} отменена", sourceNum);
                    updateBuf(bufIsBusy, value, sourceNum, amountInBuf);
                    break;
                case PUT_TO_BUFFER:
                    logger.info("Заявка источника {} помещена в буфер", sourceNum);
                    putToBufEnd(bufIsBusy, sourceNum, amountInBuf);
                    amountInBuf++;
                    break;
                case REMOVE_FROM_BUFFER:
                    logger.info("Заявка источника {} удалена с буфера", sourceNum);
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

    private void updateBuf(final int[] bufIsBusy,
                           final int bufPos,
                           final int sourceNum,
                           final int amountInBuf) {
        removeFromBuf(bufIsBusy, bufPos, amountInBuf);
        putToBufEnd(bufIsBusy, sourceNum, amountInBuf);
    }

    private void putToBufEnd(final int[] bufIsBusy,
                             final int sourceNum,
                             final int amountInBuf) {
        bufIsBusy[amountInBuf] = sourceNum;
    }

    private void printCurrentStepModeSituation(final int[] bufIsBusy,
                                               final int[] deviceIsBusy) {
        for (int i = 0; i < bufIsBusy.length; ++i) {
            logger.info("Буфер, позиция={} : источник={}", i, bufIsBusy[i]);
        }
        for (int i = 0; i < deviceIsBusy.length; ++i) {
            logger.info("Прибор, номер={} : источник={}", i, deviceIsBusy[i]);
        }
    }

    public Analytics modulateWork() {
        final Analytics analytics = new Analytics(amountOfSources, amountOfDevices);

        final Buffer buffer = new Buffer(bufferSize);
        final ProductionManager productionManager = new ProductionManager(amountOfSources, alpha, beta);
        final SelectionManager selectionManager = new SelectionManager(amountOfDevices, lamda);

        double currentTime = 0;

        for (int i = 0; i < requestsNumber; ++i) {
            final Pair<Double, List<Request>> nextRequestPair = productionManager.getNextRequest(currentTime);
            final List<Request> nextRequests = nextRequestPair.value;
            currentTime += nextRequestPair.key;

            for (final Request nextRequest : nextRequests) {
                logger.info("Источник №{} создал заявку №{} в {}",
                        nextRequest.getSourceNumber(), nextRequest.getNumber(), nextRequest.getInitialTime());
                logger.info("CurrentTime: {}", currentTime);
                analytics.addGeneratedRequest(nextRequest);
                final Pair<Integer, Request> canceledRequestPair = buffer.put(nextRequest);
                final Request canceledRequest = canceledRequestPair.value;
                if (canceledRequest != null) {
                    logger.info("Заявка №{} от источника №{} была отменена",
                            canceledRequest.getNumber(), canceledRequest.getSourceNumber());
                    analytics.cancelRequest(canceledRequest, nextRequest, canceledRequestPair.key);
                } else {
                    logger.info("Заявка успешно загружена в буфер без удалений!");
                    analytics.addRequestToBuffer(nextRequest, canceledRequestPair.key);
                }
            }
            List<DoneInfo> doneRequestsWithDevices = selectionManager.getDoneRequestsWithDevices(currentTime);
            processDoneRequest(doneRequestsWithDevices, selectionManager, buffer, currentTime, analytics);

        }

        analytics.setFullTimeOfWork(currentTime);
        analytics.calcTimeInSystem();
        return analytics;
    }

    private void processDoneRequest(final List<DoneInfo> doneRequestsWithDevices,
                                    final SelectionManager selectionManager,
                                    final Buffer buffer,
                                    final double currentTime,
                                    final Analytics analytics) {
        for (final DoneInfo doneInfo : doneRequestsWithDevices) {
            final Request doneRequest = doneInfo.doneRequest;
            if (doneRequest != null) {
                logger.info("Прибор №{} освободился в {}, выполнив {} запрос источника №{}",
                        doneInfo.deviceNumber, doneInfo.doneTime, doneRequest.getNumber(), doneRequest.getSourceNumber());
                analytics.addDoneRequest(doneInfo.deviceNumber, doneRequest, doneInfo.doneTime, doneInfo.timeOfWork);
            }
            if (!buffer.isEmpty()) {
                final int packageNumber = selectionManager.getPackageNumber();
                Pair<Integer, Request> requestFromBufPair;
                if (packageNumber != -1) {
                    requestFromBufPair = buffer.getPackageRequest(packageNumber);
                    if (requestFromBufPair == null) {
                        requestFromBufPair = buffer.getPriorityRequest();
                        selectionManager.setPackageNumber(requestFromBufPair.value.getSourceNumber());
                    }
                } else {
                    requestFromBufPair = buffer.getPriorityRequest();
                    selectionManager.setPackageNumber(requestFromBufPair.value.getSourceNumber());
                }
                final Request requestFromBuf = requestFromBufPair.value;
                logger.info("DoneTime={}", doneInfo.doneTime);
                logger.info("RequestFromBuf init time={}", requestFromBuf.getInitialTime());
                logger.info("Current time={}", currentTime);
                double time;
                if (doneInfo.doneTime == -1.0) {
                    analytics.removeFromBuffer(requestFromBuf, requestFromBufPair.key, currentTime);
                    time = currentTime;
                } else {
                    analytics.removeFromBuffer(requestFromBuf, requestFromBufPair.key, doneInfo.doneTime);
                    time = doneInfo.doneTime;
                }
                final int deviceNumber = selectionManager.executeRequest(requestFromBuf, time);
                logger.info("Заявка №{} от источника №{} загружена на прибор №{}",
                        requestFromBuf.getNumber(), requestFromBuf.getSourceNumber(), deviceNumber);
                analytics.putOnDevice(deviceNumber, time, requestFromBuf.getSourceNumber());
            }
        }
    }

}

package com.github.zvladn7.controllers;

import com.github.zvladn7.components.Buffer;
import com.github.zvladn7.components.Request;
import com.github.zvladn7.managers.ProductionManager;
import com.github.zvladn7.managers.SelectionManager;
import com.github.zvladn7.managers.SelectionManager.DoneInfo;
import com.github.zvladn7.analytics.Analytics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.zvladn7.util.Pair;

import java.util.Comparator;
import java.util.List;

public class Controller {

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
                logger.info("Uсточник №{} создал заявку №{} в {}",
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
        logger.info("------------Приборы--------------");
        doneRequestsWithDevices.sort(Comparator.comparing(DoneInfo::getDoneTime));
        for (final DoneInfo doneInfo : doneRequestsWithDevices) {
            final Request doneRequest = doneInfo.doneRequest;
            if (doneRequest != null) {
                logger.info("Прибор №{} освободился в {}, выполнив {} запрос источника №{}",
                        doneInfo.deviceNumber, doneInfo.doneTime, doneRequest.getNumber(), doneRequest.getSourceNumber());
                analytics.addDoneRequest(doneInfo.deviceNumber, doneRequest, doneInfo.doneTime, doneInfo.timeOfWork);
            }
            if (!buffer.isEmpty()) {
                int packageNumber = selectionManager.getPackageNumber();
                Pair<Integer, Request> requestFromBufPair;
                if (packageNumber != -1) {
                    requestFromBufPair = buffer.getPackageRequest(packageNumber);
                    if (requestFromBufPair == null) {
                        requestFromBufPair = buffer.getPriorityRequest();
                        packageNumber = requestFromBufPair.value.getSourceNumber();
                        selectionManager.setPackageNumber(packageNumber);
                    }
                } else {
                    requestFromBufPair = buffer.getPriorityRequest();
                    packageNumber = requestFromBufPair.value.getSourceNumber();
                    selectionManager.setPackageNumber(packageNumber);
                }
                logger.info("Package buffer number: {}", packageNumber);
                final Request requestFromBuf = requestFromBufPair.value;
                logger.info("DoneTime={}", doneInfo.doneTime);
                logger.info("RequestFromBuf init time={}", requestFromBuf.getInitialTime());
                logger.info("Current time={}", currentTime);
                double time;
                if (doneInfo.doneTime == -1.0) {
                    analytics.removeFromBuffer(requestFromBuf, requestFromBufPair.key, currentTime, packageNumber);
                    time = currentTime;
                } else {
                    if (requestFromBuf.getInitialTime() > doneInfo.doneTime) {
                        analytics.removeFromBuffer(requestFromBuf, requestFromBufPair.key, requestFromBuf.getInitialTime(), packageNumber);
                        time = requestFromBuf.getInitialTime();
                    } else {
                        analytics.removeFromBuffer(requestFromBuf, requestFromBufPair.key, doneInfo.doneTime, packageNumber);
                        time = doneInfo.doneTime;
                    }
                }
                final int deviceNumber = selectionManager.executeRequest(requestFromBuf, time);
                logger.info("Заявка №{} от источника №{} загружена на прибор №{}",
                        requestFromBuf.getNumber(), requestFromBuf.getSourceNumber(), deviceNumber);
                analytics.putOnDevice(deviceNumber, time, requestFromBuf.getSourceNumber());
            }
        }
    }

}

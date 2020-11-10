package com.github.zvladn7;

import com.github.zvladn7.SelectionManager.DoneInfo;
import com.github.zvladn7.analytics.Analytics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Controller {

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    private double alpha;
    private double beta;
    private int amountOfSources;
    private int amountOfDevices;
    private int bufferSize;
    private int requestsNumber;

    private Controller() {

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


    public Analytics modulateWork() {
        final Analytics analytics = new Analytics(amountOfSources, amountOfDevices);

        final Buffer buffer = new Buffer(bufferSize);
        final ProductionManager productionManager = new ProductionManager(amountOfSources, alpha, beta);
        final SelectionManager selectionManager = new SelectionManager(amountOfDevices);

        double currentTime = 0;

        for (int i = 0; i < requestsNumber; ++i) {
            final Pair<Double, List<Request>> nextRequestPair = productionManager.getNextRequest(currentTime);
            final List<Request> nextRequests = nextRequestPair.value;
            currentTime += nextRequestPair.key;

            List<DoneInfo> doneRequestsWithDevices = selectionManager.getDoneRequestsWithDevices(currentTime);
            processDoneRequest(doneRequestsWithDevices, selectionManager, buffer, currentTime, analytics);

            for (final Request nextRequest : nextRequests){
                logger.info("Источник №{} создал заявку №{} в {}",
                        nextRequest.getSourceNumber(), nextRequest.getNumber(), nextRequest.getInitialTime());
                logger.info("CurrentTime: {}", currentTime);
                analytics.addGeneratedRequest(nextRequest);

                final Pair<Integer, Request> canceledRequestPair = buffer.put(nextRequest);
                final Request canceledRequest = canceledRequestPair.value;
                if (canceledRequest != null) {
                    logger.info("Заявка №{} от источника №{} была отменена",
                            canceledRequest.getNumber(), canceledRequest.getSourceNumber());
                    analytics.cancelRequest(canceledRequest, nextRequest.getInitialTime());
                } else {
                    logger.info("Заявка успешно загружена в буфер без удалений!");
                    analytics.addRequestToBuffer(nextRequest, canceledRequestPair.key);
                }
            }
        }

        analytics.calcTimeInSystem();
        analytics.setFullTimeOfWork(currentTime);
        analytics.printStat();
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
                analytics.removeFromBuffer(requestFromBuf, requestFromBufPair.key, doneInfo.doneTime);

                final int deviceNumber = selectionManager.executeRequest(requestFromBuf, currentTime);
                logger.info("Заявка №{} от источника №{} загружена на прибор №{}",
                        requestFromBuf.getNumber(), requestFromBuf.getSourceNumber(), deviceNumber);
                analytics.putOnDevice(deviceNumber, doneInfo.doneTime);
            }
        }
    }

}

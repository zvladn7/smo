package com.github.zvladn7;

import com.github.zvladn7.SelectionManager.DoneInfo;
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

    class Builder {

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


    public void modulateWork(final Analytics analytics) {
        final Buffer buffer = new Buffer(bufferSize);
        final ProductionManager productionManager = new ProductionManager(amountOfSources, alpha, beta);
        final SelectionManager selectionManager = new SelectionManager(amountOfDevices);

        double currentTime = 0;

        for (int i = 0; i < requestsNumber; ++i) {
            final Pair<Double, Request> nextRequestPair = productionManager.getNextRequest(currentTime);
            final Request nextRequest = nextRequestPair.value;
            currentTime += nextRequestPair.key;

            List<DoneInfo> doneRequestsWithDevices = selectionManager.getDoneRequestsWithDevices(currentTime);
            processDoneRequest(doneRequestsWithDevices, selectionManager, buffer, currentTime);

            logger.info("Источник №{} создал заявку №{} в {}",
                    nextRequest.getSourceNumber(), nextRequest.getNumber(), nextRequest.getInitialTime());

            final Request canceledRequest = buffer.put(nextRequest);
            if (canceledRequest != null) {
                logger.info("Заявка №{} от источника №{} была отменена",
                        canceledRequest.getNumber(), canceledRequest.getSourceNumber());
            } else {
                logger.info("Заявка успешно загружена в буфер без удалений!");
            }
        }
    }

    private void processDoneRequest(final List<DoneInfo> doneRequestsWithDevices,
                                    final SelectionManager selectionManager,
                                    final Buffer buffer,
                                    final double currentTime) {
        for (final DoneInfo doneInfo : doneRequestsWithDevices) {
            final Request doneRequest = doneInfo.doneRequest;
            if (doneRequest != null) {
                logger.info("Прибор №{} освободился в {}, выполнив {} запрос источника №{}",
                        doneInfo.deviceNumber, doneInfo.doneTime, doneRequest.getNumber(), doneRequest.getSourceNumber());
            }
            if (!buffer.isEmpty()) {
                final int packageNumber = selectionManager.getPackageNumber();
                Request requestFromBuf;
                if (packageNumber != -1) {
                    requestFromBuf = buffer.getPackageRequest(packageNumber);
                    if (requestFromBuf == null) {
                        requestFromBuf = buffer.getPriorityRequest();
                        selectionManager.setPackageNumber(requestFromBuf.getSourceNumber());
                    }
                } else {
                    requestFromBuf = buffer.getPriorityRequest();
                    selectionManager.setPackageNumber(requestFromBuf.getSourceNumber());
                }

                final int deviceNumber = selectionManager.executeRequest(requestFromBuf, currentTime);
                logger.info("Заявка №{} от источника №{} загружена на прибор №{}",
                        requestFromBuf.getNumber(), requestFromBuf.getSourceNumber(), deviceNumber);
            }
        }
    }



}
package com.github.zvladn7.components;

import com.github.zvladn7.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedList;

public class Buffer {

    private static final Logger logger = LoggerFactory.getLogger(Buffer.class);

    final private LinkedList<Request> requests;
    final private int capacity;
    private int packageAmount = 0;

    public Buffer(final int capacity) {
        this.capacity = capacity;
        requests = new LinkedList<>();
    }

    public Pair<Integer, Request> put(final Request request) {
        if (isFull()) {
            return getLessPriority(request);
        }
        requests.add(request);
        return new Pair<>(requests.indexOf(request), null);
    }

    public Pair<Integer, Request> getPriorityRequest() {
        int theMostPriorSource = Integer.MAX_VALUE;
        Request prior = null;
        for (final Request request : requests) {
            if (request.getSourceNumber() < theMostPriorSource) {
               prior = request;
               theMostPriorSource = request.getSourceNumber();
            }
        }
        assert prior != null;
        final int index = requests.indexOf(prior);
        requests.remove(prior);
        for (final Request request : requests) {
            if (request.getSourceNumber() == prior.getSourceNumber()) {
                packageAmount++;
            }
        }
        logger.info("Amount of requests in package: {}, package = {}", packageAmount, prior.getSourceNumber());
        print();
        return new Pair<>(index, prior);
    }

    /**
     * Return the next Request from the package or null if there is no more requests from this package number.
     * @param packageNumber - number of the package that will be processed on devices
     * @return request from the package or null
     */
    public Pair<Integer, Request> getPackageRequest(final int packageNumber) {
        if (packageAmount == 0) {
            return null;
        }
        for (final Request request : requests) {
            if (request.getSourceNumber() == packageNumber) {
                logger.info("Следующий запрос из пакета №{} : {}", request.getSourceNumber(), request.getNumber());
                final int index = requests.indexOf(request);
                requests.remove(request);
                packageAmount--;
                logger.info("Amount of requests in package: {}, package = {}", packageAmount, request.getSourceNumber());
                print();
                return new Pair<>(index, request);
            }
        }

        return null;
    }

    private Pair<Integer, Request> getLessPriority(final Request newRequest) {
        Request lessPriority = newRequest;
        for (final Request next : requests) {
            final int compareResult = Request.REQUEST_COMPARATOR.compare(next, lessPriority);
            if (compareResult > 0) {
                lessPriority = next;
            }
        }
        logger.info("Return less priority value: src={}, num={}, initTime={}",
                lessPriority.getSourceNumber(), lessPriority.getNumber(), lessPriority.getInitialTime());
        if (lessPriority == newRequest) {
            return new Pair<>(-1, newRequest);
        } else {
            final int i = requests.indexOf(lessPriority);
            requests.remove(lessPriority);
            requests.add(newRequest);
            return new Pair<>(i, lessPriority);
        }
    }

    private boolean isFull() {
        return capacity == requests.size();
    }

    public boolean isEmpty() {
        return requests.isEmpty();
    }

    private void print() {
        StringBuilder buf = new StringBuilder("[");
        for (final Request request : requests) {
            buf.append(request.getSourceNumber()).append(" ");
        }
        buf.append("]");
        logger.info("Buffer: {}", buf);
    }

}

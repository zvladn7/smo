package com.github.zvladn7;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

public class Buffer {

    private static final Logger logger = LoggerFactory.getLogger(Buffer.class);

    final private LinkedList<Request> requests;
    final private int capacity;

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
        return new Pair<>(0, requests.poll());
    }

    /**
     * Return the next Request from the package or null if there is no more requests from this package number.
     * @param packageNumber - number of the package that will be processed on devices
     * @return request from the package or null
     */
    public Pair<Integer, Request> getPackageRequest(final int packageNumber) {
        for (final Request request : requests) {
            if (request.getSourceNumber() == packageNumber) {
                logger.info("Следующий запрос из пакета №{} : {}", request.getSourceNumber(), request.getNumber());
                final int index = requests.indexOf(request);
                requests.remove(request);
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
//            requests.set(i, newRequest);
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

    public static void main(String[] args) {
        Buffer buffer = new Buffer(5);
        buffer.put(new Request(1,1,1));
        buffer.put(new Request(1, 2, 2));
        buffer.put(new Request(2, 1, 1));
        buffer.put(new Request(2, 2, 2));
        buffer.put(new Request(2, 3, 3));
//        buffer.put(new Request(1, 3, 3));
//        buffer.put(new Request(1, 4, 4));
//        buffer.put(new Request(1, 5, 5));
        System.out.println(buffer.isFull());
//        System.out.println(buffer.getPackageRequest(2));
//        System.out.println(buffer.getPackageRequest(2));
//        System.out.println(buffer.getPackageRequest(2));
        System.out.println(buffer.getLessPriority(new Request(3, 1, 1)).key);
        System.out.println(buffer.getLessPriority(new Request(3, 2, 1)).key);
        System.out.println(buffer.getLessPriority(new Request(3, 3, 1)).key);
        System.out.println(buffer.getLessPriority(new Request(1, 3, 1)).key);
        System.out.println(buffer.getLessPriority(new Request(1, 3, 1)).key);
        System.out.println(buffer.getLessPriority(new Request(1, 3, 1)).key);
    }

}

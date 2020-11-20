package com.github.zvladn7.components;

import java.util.Comparator;

public class Request {

    static final Comparator<Request> REQUEST_COMPARATOR
            = Comparator.comparing(Request::getSourceNumber).thenComparing(Request::getNumber);

    private final int sourceNumber;
    private final int number;
    private final double initialTime;


    public Request(final int sourceNumber,
                   final int number,
                   final double initialTime) {
        this.sourceNumber = sourceNumber;
        this.number = number;
        this.initialTime = initialTime;
    }

    public double getInitialTime() {
        return initialTime;
    }

    public int getSourceNumber() {
        return sourceNumber;
    }

    public int getNumber() {
        return number;
    }

}

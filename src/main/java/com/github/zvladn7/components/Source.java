package com.github.zvladn7.components;

import com.github.zvladn7.util.Pair;

import java.util.Random;

public class Source {

    private final Random generator = new Random();

    private final int number;
    private final double alpha;
    private final double beta;
    private int nextRequestNumber;

    public Source(int number, double alpha, double beta) {
        this.number = number;
        this.alpha = alpha;
        this.beta = beta;
    }

    public Pair<Double, Request> generate(final double currentTime) {
        final double nextRequestTime = getNextRequestTime();
        return new Pair<>(nextRequestTime, new Request(number, nextRequestNumber++, currentTime));
    }

    private double getNextRequestTime() {
        return generator.nextDouble()  * (beta - alpha) + alpha;
    }

}

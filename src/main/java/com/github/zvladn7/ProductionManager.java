package com.github.zvladn7;

public class ProductionManager {

    private final Source[] sources;
    private final Request[] sourcesRequests;
    private final double[] timesToWait;

    public ProductionManager(final int amountOfSources,
                             final double alpha,
                             final double beta) {
        this.sources = new Source[amountOfSources];
        this.sourcesRequests = new Request[amountOfSources];
        this.timesToWait = new double[amountOfSources];
        initSources(alpha, beta);
    }

    private void initSources(final double alpha, final double beta) {
        for (int i = 0; i < sources.length; ++i) {
            sources[i] = new Source(i, alpha, beta);
        }
    }

    public Pair<Double, Request> getNextRequest(final double currentTime) {
        generateRequestsIfNeed(currentTime);
        final int minTimeIndex = getMinTimeIndex();
        final double minWaitTime = timesToWait[minTimeIndex];
        declineWaitTime(minTimeIndex);
        return new Pair<>(minWaitTime, sourcesRequests[minTimeIndex]);
    }

    private void declineWaitTime(final int minTimeIndex) {
        final double waitTime = timesToWait[minTimeIndex];
        for (int i = 0; i < sources.length; ++i) {
            timesToWait[i] -= waitTime;
        }
    }

    private int getMinTimeIndex() {
        int minIndex = 0;
        for (int i = 1; i < sources.length; ++i) {
            if (timesToWait[i] < timesToWait[minIndex]) {
                minIndex = i;
            }
        }
        return minIndex;
    }

    private void generateRequestsIfNeed(final double currentTime) {
        for (int i = 0; i < sources.length; ++i) {
            if (timesToWait[i] <= 0) {
                final Pair<Double, Request> generatedRequest = sources[i].generate(currentTime);
                final double timeToWait = generatedRequest.key;
                final Request newRequest = generatedRequest.value;
                sourcesRequests[i] = newRequest;
                timesToWait[i] = timeToWait;
            }
        }
    }

}

package com.sap.sailing.windestimation.aggregator.hmm;

public class BestNodeInfo {

    private double probabilityFromStart;
    private double forwardProbability;
    private double backwardProbability;

    public BestNodeInfo(double probabilityFromStart) {
        this.probabilityFromStart = probabilityFromStart;
    }

    public double getProbabilityFromStart() {
        return probabilityFromStart;
    }

    public void setProbabilityFromStart(double probabilityFromStart) {
        this.probabilityFromStart = probabilityFromStart;
    }

    public double getForwardProbability() {
        return forwardProbability;
    }

    public void setForwardProbability(double forwardProbability) {
        this.forwardProbability = forwardProbability;
    }

    public double getBackwardProbability() {
        return backwardProbability;
    }

    public void setBackwardProbability(double backwardProbability) {
        this.backwardProbability = backwardProbability;
    }

}

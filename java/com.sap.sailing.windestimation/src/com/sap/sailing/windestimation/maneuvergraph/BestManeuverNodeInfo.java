package com.sap.sailing.windestimation.maneuvergraph;

public class BestManeuverNodeInfo {

    private final GraphNode bestPreviousNode;
    private double probabilityFromStart;
    private double forwardProbability;
    private double backwardProbability;
    private final IntersectedWindRange windRange;

    public BestManeuverNodeInfo(GraphNode bestPreviousNode, double probabilityFromStart,
            IntersectedWindRange windRange) {
        this.bestPreviousNode = bestPreviousNode;
        this.probabilityFromStart = probabilityFromStart;
        this.windRange = windRange;
    }

    public GraphNode getBestPreviousNode() {
        return bestPreviousNode;
    }

    public IntersectedWindRange getWindRange() {
        return windRange;
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

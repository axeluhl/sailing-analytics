package com.sap.sailing.windestimation.maneuvergraph;

import java.util.HashMap;
import java.util.Map;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.windestimation.polarsfitting.SailingStatistics;

public class BestManeuverNodeInfo {

    private final GraphNode bestPreviousNode;
    private double probabilityFromStart;
    private double forwardProbability;
    private double backwardProbability;
    private final IntersectedWindRange windRange;
    private final Map<String, SailingStatistics> pathSailingStatisticsPerBoatClassName = new HashMap<>();

    public BestManeuverNodeInfo(GraphNode bestPreviousNode, double probabilityFromStart,
            IntersectedWindRange windRange) {
        this.bestPreviousNode = bestPreviousNode;
        this.probabilityFromStart = probabilityFromStart;
        this.windRange = windRange;
    }

    public SailingStatistics getPathSailingStatistics(BoatClass boatClass) {
        return pathSailingStatisticsPerBoatClassName.get(boatClass.getName());
    }

    public void setPathSailingStatistics(BoatClass boatClass, SailingStatistics sailingStatistics) {
        pathSailingStatisticsPerBoatClassName.put(boatClass.getName(), sailingStatistics);
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

package com.sap.sailing.windestimation.aggregator.hmm;

public class BestManeuverNodeInfo extends BestNodeInfo {

    private final GraphNode bestPreviousNode;

    public BestManeuverNodeInfo(GraphNode bestPreviousNode, double probabilityFromStart,
            IntersectedWindRange intersectedWindRange) {
        super(probabilityFromStart, intersectedWindRange);
        this.bestPreviousNode = bestPreviousNode;
    }

    public GraphNode getBestPreviousNode() {
        return bestPreviousNode;
    }

}

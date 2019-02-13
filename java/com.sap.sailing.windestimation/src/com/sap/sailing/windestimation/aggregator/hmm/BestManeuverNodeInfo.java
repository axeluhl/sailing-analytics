package com.sap.sailing.windestimation.aggregator.hmm;

/**
 * Contains information about best path until the node from level represented in {@link BestPathsPerLevel}.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
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

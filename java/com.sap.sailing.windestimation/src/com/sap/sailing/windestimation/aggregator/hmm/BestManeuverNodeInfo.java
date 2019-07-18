package com.sap.sailing.windestimation.aggregator.hmm;

/**
 * Contains information about best path until the node from level represented in {@link BestPathsPerLevel}.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class BestManeuverNodeInfo<GL extends GraphLevelBase<GL>> extends BestNodeInfo {

    private final GraphNode<GL> bestPreviousNode;

    public BestManeuverNodeInfo(GraphNode<GL> bestPreviousNode, double probabilityFromStart,
            IntersectedWindRange intersectedWindRange) {
        super(probabilityFromStart, intersectedWindRange);
        this.bestPreviousNode = bestPreviousNode;
    }

    public GraphNode<GL> getBestPreviousNode() {
        return bestPreviousNode;
    }

}

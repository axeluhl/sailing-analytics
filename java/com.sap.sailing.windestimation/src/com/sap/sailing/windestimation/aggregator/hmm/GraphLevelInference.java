package com.sap.sailing.windestimation.aggregator.hmm;

/**
 * Represents inference result for a {@link GraphLevelBase} within HMM. For a "level" (node) in the overarching graph
 * which contains {@link GraphNode} objects for each maneuver classification hypothesis this object tells which of those
 * has been selected as the most probable, as well as the "confidence" (a value between 0 and 1 with 0 meaning highly
 * unlikely and 1 meaning pretty certain).
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class GraphLevelInference<GL extends GraphLevelBase<GL>> {

    private final GraphNode<GL> graphNode;
    private final double confidence;

    public GraphLevelInference(GraphNode<GL> graphNode, double confidence) {
        this.graphNode = graphNode;
        this.confidence = confidence;
    }

    public GL getGraphLevel() {
        return getGraphNode().getGraphLevel();
    }

    public GraphNode<GL> getGraphNode() {
        return graphNode;
    }

    public double getConfidence() {
        return confidence;
    }

    @Override
    public String toString() {
        return "" + graphNode + " at " + getGraphLevel().getManeuver().getManeuverTimePoint() + " "
                + getGraphLevel().getManeuver().getManeuverPosition() + ", confidence=" + confidence + "]";
    }
}

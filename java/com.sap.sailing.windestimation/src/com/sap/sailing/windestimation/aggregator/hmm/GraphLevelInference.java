package com.sap.sailing.windestimation.aggregator.hmm;

/**
 * Represents inference result for a {@link GraphLevelBase} within HMM.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class GraphLevelInference {

    private final GraphLevelBase graphLevel;
    private final GraphNode graphNode;
    private final double confidence;

    public GraphLevelInference(GraphLevelBase graphLevel, GraphNode graphNode, double confidence) {
        this.graphLevel = graphLevel;
        this.graphNode = graphNode;
        this.confidence = confidence;
    }

    public GraphLevelBase getGraphLevel() {
        return graphLevel;
    }

    public GraphNode getGraphNode() {
        return graphNode;
    }

    public double getConfidence() {
        return confidence;
    }

}

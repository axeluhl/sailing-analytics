package com.sap.sailing.windestimation.impl.maneuvergraph;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class NodeTransitionProperties {

    private FineGrainedPointOfSail bestPreviousNode;
    private double probabilityOfBestPathToNodeFromStart;
    private double[] probabilitiesFromPreviousNodesLevel = new double[FineGrainedPointOfSail.values().length];

    public FineGrainedPointOfSail getBestPreviousNode() {
        return bestPreviousNode;
    }

    public void setBestPreviousNode(FineGrainedPointOfSail bestPreviousNode,
            double probabilityOfBestPathToNodeFromStart) {
        this.bestPreviousNode = bestPreviousNode;
        this.probabilityOfBestPathToNodeFromStart = probabilityOfBestPathToNodeFromStart;
    }

    public double getProbabilityOfBestPathToNodeFromStart() {
        return probabilityOfBestPathToNodeFromStart;
    }

    public double getProbabilitiesFromPreviousNodesLevel(FineGrainedPointOfSail previousNode) {
        return probabilitiesFromPreviousNodesLevel[previousNode.ordinal()];
    }

    public void setProbabilitiesFromPreviousNodesLevel(FineGrainedPointOfSail previousNode,
            double probabilityToThisNode) {
        this.probabilitiesFromPreviousNodesLevel[previousNode.ordinal()] = probabilityToThisNode;
    }

}

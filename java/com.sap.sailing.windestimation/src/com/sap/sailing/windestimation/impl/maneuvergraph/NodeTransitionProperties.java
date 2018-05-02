package com.sap.sailing.windestimation.impl.maneuvergraph;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class NodeTransitionProperties {

    private FineGrainedPointOfSail bestPreviousNode;
    private double bestDistanceFromStart;
    private double[] distancesFromPreviousNodesLevel = new double[FineGrainedPointOfSail.values().length];
    
    public FineGrainedPointOfSail getBestPreviousNode() {
        return bestPreviousNode;
    }
    public void setBestPreviousNode(FineGrainedPointOfSail bestPreviousNode, double bestDistanceFromStart) {
        this.bestPreviousNode = bestPreviousNode;
        this.bestDistanceFromStart = bestDistanceFromStart;
    }
    public double getBestDistanceFromStart() {
        return bestDistanceFromStart;
    }
    public double getDistancesFromPreviousNodesLevel(FineGrainedPointOfSail previousNode) {
        return distancesFromPreviousNodesLevel[previousNode.ordinal()];
    }
    public void setDistancesFromPreviousNodesLevel(FineGrainedPointOfSail previousNode, double distanceToThisNode) {
        this.distancesFromPreviousNodesLevel[previousNode.ordinal()] = distanceToThisNode;
    }
    
    
    
}

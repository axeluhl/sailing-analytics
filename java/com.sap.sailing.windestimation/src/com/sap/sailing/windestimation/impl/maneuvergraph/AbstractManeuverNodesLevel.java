package com.sap.sailing.windestimation.impl.maneuvergraph;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public abstract class AbstractManeuverNodesLevel<SelfType extends AbstractManeuverNodesLevel<SelfType>> implements ManeuverNodesLevel<SelfType> {

    private final CompleteManeuverCurveWithEstimationData maneuver;
    
    private SelfType previousLevel = null;
    private SelfType nextLevel = null;

    private final FineGrainedPointOfSail[] bestPreviousNodesForTheseNodes = new FineGrainedPointOfSail[FineGrainedPointOfSail
            .values().length];
    private final double[][] distancesFromPreviousNodesToTheseNodes = new double[bestPreviousNodesForTheseNodes.length][bestPreviousNodesForTheseNodes.length];
    private final double[] bestDistancesFromStartToTheseNodes = new double[bestPreviousNodesForTheseNodes.length];

    public AbstractManeuverNodesLevel(CompleteManeuverCurveWithEstimationData maneuver) {
        this.maneuver = maneuver;
    }

    @Override
    public CompleteManeuverCurveWithEstimationData getManeuver() {
        return maneuver;
    }

    @Override
    public double getDistanceToNodeFromStart(FineGrainedPointOfSail node) {
        return bestDistancesFromStartToTheseNodes[node.ordinal()];
    }

    @Override
    public FineGrainedPointOfSail getBestPreviousNode(FineGrainedPointOfSail node) {
        return bestPreviousNodesForTheseNodes[node.ordinal()];
    }

    public FineGrainedPointOfSail[] getBestPreviousNodesForThisNodes() {
        return bestPreviousNodesForTheseNodes;
    }

    @Override
    public double[] getBestDistancesFromStart() {
        return bestDistancesFromStartToTheseNodes;
    }

    public double getDistanceFromPreviousLevelNodeToThisLevelNode(FineGrainedPointOfSail previousLevelNode,
            FineGrainedPointOfSail thisLevelNode) {
        return distancesFromPreviousNodesToTheseNodes[previousLevelNode.ordinal()][thisLevelNode.ordinal()];
    }
    
    protected void setNextLevel(SelfType nextLevel) {
        this.nextLevel = nextLevel;
    }

    @Override
    public SelfType getNextLevel() {
        return nextLevel;
    }

    protected void setPreviousLevel(SelfType previousLevel) {
        this.previousLevel = previousLevel;
    }

    @Override
    public SelfType getPreviousLevel() {
        return previousLevel;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void appendNextManeuverNodesLevel(SelfType nextManeuverNodesLevel) {
        setNextLevel(nextManeuverNodesLevel);
        nextManeuverNodesLevel.setPreviousLevel((SelfType) this);
    }

}

package com.sap.sailing.windestimation.impl.maneuvergraph;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public abstract class AbstractManeuverNodesLevel<SelfType extends AbstractManeuverNodesLevel<SelfType>>
        implements ManeuverNodesLevel<SelfType> {

    private final CompleteManeuverCurveWithEstimationData maneuver;

    private SelfType previousLevel = null;
    private SelfType nextLevel = null;

    protected final FineGrainedPointOfSail[] bestPreviousNodesForTheseNodes = new FineGrainedPointOfSail[FineGrainedPointOfSail
            .values().length];
    protected final double[][] distancesFromPreviousNodesToTheseNodes = new double[bestPreviousNodesForTheseNodes.length][bestPreviousNodesForTheseNodes.length];
    protected final double[] bestDistancesFromStartToTheseNodes = new double[bestPreviousNodesForTheseNodes.length];

    public AbstractManeuverNodesLevel(CompleteManeuverCurveWithEstimationData maneuver) {
        this.maneuver = maneuver;
    }

    @Override
    public CompleteManeuverCurveWithEstimationData getManeuver() {
        return maneuver;
    }

    @Override
    public FineGrainedPointOfSail getBestPreviousNode(FineGrainedPointOfSail toNode) {
        return bestPreviousNodesForTheseNodes[toNode.ordinal()];
    }

    @Override
    public double getBestDistanceToNodeFromStart(FineGrainedPointOfSail toNode) {
        return bestDistancesFromStartToTheseNodes[toNode.ordinal()];
    }

    @Override
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

    @Override
    public void computeBestPathsToThisLevel() {
        SelfType previousLevel = getPreviousLevel();
        if (previousLevel != null) {
            for (FineGrainedPointOfSail previousNode : FineGrainedPointOfSail.values()) {
                for (FineGrainedPointOfSail currentNode : FineGrainedPointOfSail.values()) {
                    double distanceFromStart = previousLevel.getBestDistanceToNodeFromStart(previousNode)
                            + this.getDistanceFromPreviousLevelNodeToThisLevelNode(previousNode, currentNode)
                                    * getPenaltyFactorForTransitionConsideringWholeBestPath(previousNode, currentNode);
                    double existingBestDistanceToNodeFromStart = this.getBestDistanceToNodeFromStart(currentNode);
                    if (existingBestDistanceToNodeFromStart == 0
                            || distanceFromStart < existingBestDistanceToNodeFromStart) {
                        this.bestDistancesFromStartToTheseNodes[currentNode.ordinal()] = distanceFromStart;
                        this.bestPreviousNodesForTheseNodes[currentNode.ordinal()] = previousNode;
                    }
                }
            }
        }
    }

    protected double getPenaltyFactorForTransitionConsideringWholeBestPath(FineGrainedPointOfSail previousNode,
            FineGrainedPointOfSail currentNode) {
        // TODO consider average speed/course as actual polars, comparing tacks vs. jibes regarding average
        // lowest speed, max/avg turning rate, course change (if not mark passing), maneuver time loss (if
        // not mark passing)
        return 0;
    }

}

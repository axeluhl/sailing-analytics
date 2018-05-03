package com.sap.sailing.windestimation.impl.maneuvergraph;

import com.sap.sailing.domain.common.Bearing;
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

    protected NodeTransitionProperties[] nodeTransitions = new NodeTransitionProperties[FineGrainedPointOfSail
            .values().length];

    public AbstractManeuverNodesLevel(CompleteManeuverCurveWithEstimationData maneuver) {
        this.maneuver = maneuver;
    }

    @Override
    public CompleteManeuverCurveWithEstimationData getManeuver() {
        return maneuver;
    }

    @Override
    public FineGrainedPointOfSail getBestPreviousNode(FineGrainedPointOfSail toNode) {
        return nodeTransitions[toNode.ordinal()].getBestPreviousNode();
    }

    @Override
    public double getProbabilityOfBestPathToNodeFromStart(FineGrainedPointOfSail toNode) {
        return nodeTransitions[toNode.ordinal()].getProbabilityOfBestPathToNodeFromStart();
    }

    @Override
    public double getProbabilityFromPreviousLevelNodeToThisLevelNode(FineGrainedPointOfSail previousLevelNode,
            FineGrainedPointOfSail thisLevelNode) {
        return nodeTransitions[thisLevelNode.ordinal()].getProbabilitiesFromPreviousNodesLevel(previousLevelNode);
    }

    protected void normalizeNodeTransitions() {
        for (NodeTransitionProperties nodeTransition : nodeTransitions) {
            double probabilitiesSum = 0;
            for (FineGrainedPointOfSail previousNode : FineGrainedPointOfSail.values()) {
                probabilitiesSum += nodeTransition.getProbabilitiesFromPreviousNodesLevel(previousNode);
            }
            for (FineGrainedPointOfSail previousNode : FineGrainedPointOfSail.values()) {
                double normalizedTransitionProbability = nodeTransition
                        .getProbabilitiesFromPreviousNodesLevel(previousNode) / probabilitiesSum;
                nodeTransition.setProbabilitiesFromPreviousNodesLevel(previousNode, normalizedTransitionProbability);
            }
        }
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
                    double probabilityOfBestPathToNodeFromStart = previousLevel
                            .getProbabilityOfBestPathToNodeFromStart(previousNode)
                            * this.getProbabilityFromPreviousLevelNodeToThisLevelNode(previousNode, currentNode)
                            * getPenaltyFactorForTransitionConsideringWholeBestPath(previousNode, currentNode);
                    double existingProbabilityOfBestPathToNodeFromStart = this
                            .getProbabilityOfBestPathToNodeFromStart(currentNode);
                    if (probabilityOfBestPathToNodeFromStart > existingProbabilityOfBestPathToNodeFromStart) {
                        this.nodeTransitions[currentNode.ordinal()].setBestPreviousNode(previousNode,
                                probabilityOfBestPathToNodeFromStart);
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

    public Bearing getCourse() {
        return getManeuver().getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getBearing();
    }

}

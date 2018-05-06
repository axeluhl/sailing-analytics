package com.sap.sailing.windestimation.maneuvergraph;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NauticalSide;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class SingleTrackManeuverNodesLevel extends AbstractManeuverNodesLevel<SingleTrackManeuverNodesLevel> {

    private final SingleManeuverClassificationResult maneuverClassificationResult;
    private final BoatClass boatClass;

    public SingleTrackManeuverNodesLevel(SingleManeuverClassificationResult singleManeuverClassificationResult,
            BoatClass boatClass) {
        super(singleManeuverClassificationResult.getManeuver());
        maneuverClassificationResult = singleManeuverClassificationResult;
        this.boatClass = boatClass;
    }

    @Override
    public void computeProbabilitiesFromPreviousLevelToThisLevel() {
        for (FineGrainedPointOfSail currentNode : FineGrainedPointOfSail.values()) {
            double likelihoodForCurrentNode = maneuverClassificationResult
                    .getLikelihoodForPointOfSailAfterManeuver(currentNode.getCoarseGrainedPointOfSail());
            if (getPreviousLevel() == null) {
                this.nodeTransitions[currentNode.ordinal()].setBestPreviousNode(null, likelihoodForCurrentNode);
            } else {
                for (FineGrainedPointOfSail previousNode : FineGrainedPointOfSail.values()) {
                    double likelihoodForTransitionFromPreviousNodeToCurrentNode = likelihoodForCurrentNode
                            * getNodeTransitionPenaltyFactor(previousNode, currentNode);
                    nodeTransitions[currentNode.ordinal()].setProbabilitiesFromPreviousNodesLevel(previousNode,
                            likelihoodForTransitionFromPreviousNodeToCurrentNode);
                }
            }
        }
        normalizeNodeTransitions();
    }

    private double getNodeTransitionPenaltyFactor(FineGrainedPointOfSail previousNode,
            FineGrainedPointOfSail currentNode) {
        double courseDifference = getPreviousLevel().getCourse().getDifferenceTo(getCourse()).getDegrees();
        FineGrainedPointOfSail targetPointOfSail = previousNode.getNextPointOfSail(courseDifference);
        if (targetPointOfSail == currentNode) {
            return 1;
        }
        if (targetPointOfSail.getCoarseGrainedPointOfSail() == currentNode.getCoarseGrainedPointOfSail()) {
            return 1 / (1 + Math.abs(courseDifference / 45));
        }
        if (targetPointOfSail.getTack() == currentNode.getTack() && (targetPointOfSail.getLegType() == LegType.REACHING
                || currentNode.getLegType() == LegType.REACHING)) {
            return 1 / (1 + Math.abs(courseDifference / 30));
        }
        if (targetPointOfSail.getNextPointOfSail(NauticalSide.STARBOARD) == currentNode
                || targetPointOfSail.getNextPointOfSail(NauticalSide.PORT) == currentNode) {
            return 1 / (1 + Math.abs((courseDifference) / 15));
        }
        return 1 / (1 + Math.pow((courseDifference) / 15, 2));
    }

    public static ManeuverNodesLevelFactory<SingleTrackManeuverNodesLevel, SingleManeuverClassificationResult> getFactory(
            BoatClass boatClass) {
        return new ManeuverNodesLevelFactory<SingleTrackManeuverNodesLevel, SingleManeuverClassificationResult>() {

            @Override
            public SingleTrackManeuverNodesLevel createNewManeuverNodesLevel(
                    SingleManeuverClassificationResult singleManeuverClassificationResult) {
                return new SingleTrackManeuverNodesLevel(singleManeuverClassificationResult, boatClass);
            }
        };
    }

    @Override
    public BoatClass getBoatClass() {
        return boatClass;
    }

}

package com.sap.sailing.windestimation.maneuvergraph;

import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NauticalSide;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class SingleTrackManeuverNodesLevel extends AbstractManeuverNodesLevel<SingleTrackManeuverNodesLevel> {

    private final SingleManeuverClassificationResult maneuverClassificationResult;

    public SingleTrackManeuverNodesLevel(SingleManeuverClassificationResult singleManeuverClassificationResult) {
        super(singleManeuverClassificationResult.getManeuver());
        maneuverClassificationResult = singleManeuverClassificationResult;
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
                            * getNodeTransitionPenaltyFactor(getPreviousLevel(), previousNode, this, currentNode);
                    nodeTransitions[currentNode.ordinal()].setProbabilitiesFromPreviousNodesLevel(previousNode,
                            likelihoodForTransitionFromPreviousNodeToCurrentNode);
                }
            }
        }
        normalizeNodeTransitions();
    }

    private double getNodeTransitionPenaltyFactor(SingleTrackManeuverNodesLevel previousLevel,
            FineGrainedPointOfSail previousNode, SingleTrackManeuverNodesLevel singleTrackManeuverNodesLevel,
            FineGrainedPointOfSail currentNode) {
        double courseDifference = previousLevel.getCourse().getDifferenceTo(singleTrackManeuverNodesLevel.getCourse())
                .getDegrees();
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

    public static ManeuverNodesLevelFactory<SingleTrackManeuverNodesLevel, SingleManeuverClassificationResult> getFactory() {
        return new ManeuverNodesLevelFactory<SingleTrackManeuverNodesLevel, SingleManeuverClassificationResult>() {

            @Override
            public SingleTrackManeuverNodesLevel createNewManeuverNodesLevel(
                    SingleManeuverClassificationResult singleManeuverClassificationResult) {
                return new SingleTrackManeuverNodesLevel(singleManeuverClassificationResult);
            }
        };
    }

}

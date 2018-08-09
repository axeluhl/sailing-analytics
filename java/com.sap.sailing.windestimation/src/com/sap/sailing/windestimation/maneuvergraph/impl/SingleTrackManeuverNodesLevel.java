package com.sap.sailing.windestimation.maneuvergraph.impl;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.windestimation.maneuvergraph.FineGrainedPointOfSail;
import com.sap.sailing.windestimation.maneuvergraph.ManeuverNodesLevelFactory;
import com.sap.sailing.windestimation.maneuvergraph.impl.classifier.ManeuverClassificationResult;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class SingleTrackManeuverNodesLevel extends AbstractManeuverNodesLevel<SingleTrackManeuverNodesLevel> {

    private final ManeuverClassificationResult maneuverClassificationResult;
    private boolean calculationOfTransitionProbabilitiesNeeded = true;

    public SingleTrackManeuverNodesLevel(ManeuverClassificationResult singleManeuverClassificationResult) {
        super(singleManeuverClassificationResult.getManeuver());
        maneuverClassificationResult = singleManeuverClassificationResult;
    }

    @Override
    public void computeProbabilitiesFromPreviousLevelToThisLevel() {
        for (FineGrainedPointOfSail currentNode : FineGrainedPointOfSail.values()) {
            double likelihoodForCurrentNode = maneuverClassificationResult
                    .getLikelihoodForPointOfSailAfterManeuver(currentNode.getCoarseGrainedPointOfSail());
            if (getPreviousLevel() == null) {
                setProbabilityFromPreviousLevelNodeToThisLevelNode(null, currentNode, likelihoodForCurrentNode);
            } else {
                for (FineGrainedPointOfSail previousNode : FineGrainedPointOfSail.values()) {
                    double likelihoodForTransitionFromPreviousNodeToCurrentNode = likelihoodForCurrentNode
                            * getNodeTransitionPenaltyFactor(previousNode, currentNode)
                            * getBonusFactorForSymmetricTransition(previousNode, currentNode);
                    setProbabilityFromPreviousLevelNodeToThisLevelNode(previousNode, currentNode,
                            likelihoodForTransitionFromPreviousNodeToCurrentNode);
                }
            }
        }
        normalizeNodeTransitions();
        calculationOfTransitionProbabilitiesNeeded = false;
    }

    private double getBonusFactorForSymmetricTransition(FineGrainedPointOfSail previousNode,
            FineGrainedPointOfSail currentNode) {
        int previousAbsTwa = previousNode.getTwa();
        if (previousAbsTwa > 180) {
            previousAbsTwa = 360 - previousAbsTwa;
        }
        int currentAbsTwa = currentNode.getTwa();
        if (currentAbsTwa > 180) {
            currentAbsTwa = 360 - currentAbsTwa;
        }

        if (currentAbsTwa == previousAbsTwa) {
            return 1.02;
        }
        return 1;
    }

    public static ManeuverNodesLevelFactory<SingleTrackManeuverNodesLevel, ManeuverClassificationResult> getFactory() {
        return new ManeuverNodesLevelFactory<SingleTrackManeuverNodesLevel, ManeuverClassificationResult>() {

            @Override
            public SingleTrackManeuverNodesLevel createNewManeuverNodesLevel(
                    ManeuverClassificationResult singleManeuverClassificationResult) {
                return new SingleTrackManeuverNodesLevel(singleManeuverClassificationResult);
            }
        };
    }

    @Override
    public BoatClass getBoatClass() {
        return maneuverClassificationResult.getBoatClass();
    }

    @Override
    public void setTackProbabilityBonusToManeuver(double tackProbabilityBonus) {
        this.calculationOfTransitionProbabilitiesNeeded = maneuverClassificationResult
                .setTackProbabilityBonus(tackProbabilityBonus);
    }

    @Override
    public boolean isCalculationOfTransitionProbabilitiesNeeded() {
        return calculationOfTransitionProbabilitiesNeeded;
    }

    @Override
    public CompleteManeuverCurveWithEstimationData getPreviousManeuverOfSameTrack() {
        return getPreviousLevel() == null ? null : getPreviousLevel().getManeuver();
    }

    @Override
    public CompleteManeuverCurveWithEstimationData getNextManeuverOfSameTrack() {
        return getNextLevel() == null ? null : getNextLevel().getManeuver();
    }

    @Override
    public boolean isManeuverBeginningClean() {
        ManeuverClassificationResult maneuverClassificationResult = this.maneuverClassificationResult;
        if (maneuverClassificationResult == null) {
            return getManeuver().isManeuverBeginningClean(getPreviousManeuverOfSameTrack());
        }
        return maneuverClassificationResult.isCleanManeuverBeginning();
    }

    @Override
    public boolean isManeuverEndClean() {
        ManeuverClassificationResult maneuverClassificationResult = this.maneuverClassificationResult;
        if (maneuverClassificationResult == null) {
            return getManeuver().isManeuverEndClean(getNextManeuverOfSameTrack());
        }
        return maneuverClassificationResult.isCleanManeuverEnd();
    }

}

package com.sap.sailing.windestimation.maneuvergraph.impl;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NauticalSide;
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
    private final BoatClass boatClass;
    private boolean calculationOfTransitionProbabilitiesNeeded = true;

    public SingleTrackManeuverNodesLevel(ManeuverClassificationResult singleManeuverClassificationResult,
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

    private double getNodeTransitionPenaltyFactor(FineGrainedPointOfSail previousNode,
            FineGrainedPointOfSail currentNode) {
        double courseDifference = getPreviousLevel().getCourseAfter().getDifferenceTo(getCourseAfter()).getDegrees();
        FineGrainedPointOfSail targetPointOfSail = previousNode.getNextPointOfSail(courseDifference);
        if (targetPointOfSail == currentNode) {
            return 1;
        }
        double windCourseDeviation = targetPointOfSail.getDifferenceInDegrees(currentNode);
        if (targetPointOfSail.getCoarseGrainedPointOfSail() == currentNode.getCoarseGrainedPointOfSail()) {
            return 1 / (1 + Math.abs(windCourseDeviation / 45));
        }
        if (targetPointOfSail.getTack() == currentNode.getTack() && (targetPointOfSail.getLegType() == LegType.REACHING
                || currentNode.getLegType() == LegType.REACHING)) {
            return 1 / (1 + Math.abs(windCourseDeviation / 30));
        }
        if (targetPointOfSail.getNextPointOfSail(NauticalSide.STARBOARD) == currentNode
                || targetPointOfSail.getNextPointOfSail(NauticalSide.PORT) == currentNode) {
            return 1 / (1 + Math.abs((windCourseDeviation) / 15));
        }
        return 1 / (1 + Math.pow((windCourseDeviation) / 15, 2));
    }

    public static ManeuverNodesLevelFactory<SingleTrackManeuverNodesLevel, ManeuverClassificationResult> getFactory(
            BoatClass boatClass) {
        return new ManeuverNodesLevelFactory<SingleTrackManeuverNodesLevel, ManeuverClassificationResult>() {

            @Override
            public SingleTrackManeuverNodesLevel createNewManeuverNodesLevel(
                    ManeuverClassificationResult singleManeuverClassificationResult) {
                return new SingleTrackManeuverNodesLevel(singleManeuverClassificationResult, boatClass);
            }
        };
    }

    @Override
    public BoatClass getBoatClass() {
        return boatClass;
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

package com.sap.sailing.windestimation.maneuvergraph;

import java.util.ListIterator;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.windestimation.maneuvergraph.classifier.SingleManeuverClassificationResult;
import com.sap.sse.common.Util.Pair;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class SingleTrackManeuverNodesLevel extends AbstractManeuverNodesLevel<SingleTrackManeuverNodesLevel> {

    private final SingleManeuverClassificationResult maneuverClassificationResult;
    private final BoatClass boatClass;
    private double tackProbabilityBonus = 0;
    private boolean calculationOfTransitionProbabilitiesNeeded = true;

    public SingleTrackManeuverNodesLevel(SingleManeuverClassificationResult singleManeuverClassificationResult,
            BoatClass boatClass) {
        super(singleManeuverClassificationResult.getManeuver());
        maneuverClassificationResult = singleManeuverClassificationResult;
        this.boatClass = boatClass;
    }

    @Override
    public void computeProbabilitiesFromPreviousLevelToThisLevel() {
        for (FineGrainedPointOfSail currentNode : FineGrainedPointOfSail.values()) {
            double likelihoodForCurrentNode = maneuverClassificationResult.getLikelihoodForPointOfSailAfterManeuver(
                    currentNode.getCoarseGrainedPointOfSail(), getTackProbabilityBonus());
            if (getPreviousLevel() == null) {
                setProbabilityFromPreviousLevelNodeToThisLevelNode(null, currentNode, likelihoodForCurrentNode);
            } else {
                for (FineGrainedPointOfSail previousNode : FineGrainedPointOfSail.values()) {
                    double likelihoodForTransitionFromPreviousNodeToCurrentNode = likelihoodForCurrentNode
                            * getNodeTransitionPenaltyFactor(previousNode, currentNode);
                    setProbabilityFromPreviousLevelNodeToThisLevelNode(previousNode, currentNode,
                            likelihoodForTransitionFromPreviousNodeToCurrentNode);
                }
            }
        }
        normalizeNodeTransitions();
        calculationOfTransitionProbabilitiesNeeded = false;
    }

    private double getNodeTransitionPenaltyFactor(FineGrainedPointOfSail previousNode,
            FineGrainedPointOfSail currentNode) {
        double courseDifference = getPreviousLevel().getCourse().getDifferenceTo(getCourse()).getDegrees();
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

    @Override
    public Pair<SingleTrackManeuverNodesLevel, FineGrainedPointOfSail> getPreviousManeuverNodesLevelOfSameTrack(
            ListIterator<Pair<SingleTrackManeuverNodesLevel, FineGrainedPointOfSail>> iteratorWithCurrentManeuverAsNext) {
        if (iteratorWithCurrentManeuverAsNext.hasPrevious()) {
            return iteratorWithCurrentManeuverAsNext.previous();
        }
        return null;
    }

    @Override
    public void setTackProbabilityBonusToManeuver(double tackProbabilityBonus) {
        this.calculationOfTransitionProbabilitiesNeeded = Math.abs(tackProbabilityBonus - this.tackProbabilityBonus) > 0.001;
        this.tackProbabilityBonus = tackProbabilityBonus;
    }

    @Override
    public double getTackProbabilityBonus() {
        return tackProbabilityBonus;
    }
    
    @Override
    public boolean isCalculationOfTransitionProbabilitiesNeeded() {
        return calculationOfTransitionProbabilitiesNeeded;
    }

}

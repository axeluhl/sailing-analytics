package com.sap.sailing.windestimation.maneuvergraph.impl;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.windestimation.maneuvergraph.FineGrainedPointOfSail;
import com.sap.sailing.windestimation.maneuvergraph.ManeuverNodesLevel;
import com.sap.sailing.windestimation.maneuvergraph.ManeuverNodesLevelFactory;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class CrossTrackManeuverNodesLevel extends AbstractManeuverNodesLevel<CrossTrackManeuverNodesLevel>
        implements ManeuverNodesLevel<CrossTrackManeuverNodesLevel> {

    private static final double TRESHOLD_FOR_SMALL_PENALTY_WIND_COURSE_SHIFT_IN_DEGREES = 45;
    private static final double TRESHOLD_FOR_PENALTY_FREE_WIND_COURSE_SHIFT_IN_DEGREES = 10;
    private final SingleTrackManeuverNodesLevel singleTrackManeuverNodesLevel;

    private boolean calculationOfTransitionProbabilitiesNeeded = true;

    public CrossTrackManeuverNodesLevel(SingleTrackManeuverNodesLevel singleTrackManeuverNodesLevel) {
        super(singleTrackManeuverNodesLevel.getManeuver());
        this.singleTrackManeuverNodesLevel = singleTrackManeuverNodesLevel;
    }

    @Override
    public void computeProbabilitiesFromPreviousLevelToThisLevel() {
        for (FineGrainedPointOfSail previousNode : FineGrainedPointOfSail.values()) {
            CrossTrackManeuverNodesLevel previousLevel = getPreviousLevel();
            for (FineGrainedPointOfSail currentNode : FineGrainedPointOfSail.values()) {
                double probabilitiesSum = 0;
                int probabilitiesCount = 0;
                if (previousLevel != null) {
                    SingleTrackManeuverNodesLevel previousLevelNextSingleTrackLevel = previousLevel
                            .getSingleTrackManeuverNodesLevel().getNextLevel();
                    if (previousLevelNextSingleTrackLevel != null) {
                        double courseDiffBetweenThisLevelAndPreviousLevelNextSingleTrackLevel = this.getCourseAfter()
                                .getDifferenceTo(previousLevelNextSingleTrackLevel.getCourseAfter()).getDegrees();
                        FineGrainedPointOfSail nextSingleTrackNode = currentNode
                                .getNextPointOfSail(courseDiffBetweenThisLevelAndPreviousLevelNextSingleTrackLevel);
                        probabilitiesSum += previousLevelNextSingleTrackLevel
                                .getProbabilityFromPreviousLevelNodeToThisLevelNode(previousNode, nextSingleTrackNode);
                        probabilitiesCount++;
                    }
                }
                SingleTrackManeuverNodesLevel thisLevelCurrentSingleTrackLevel = this
                        .getSingleTrackManeuverNodesLevel();
                SingleTrackManeuverNodesLevel thisLevelPreviousSingleTrackLevel = thisLevelCurrentSingleTrackLevel
                        .getPreviousLevel();
                FineGrainedPointOfSail previousSingleTrackNode;
                if (thisLevelPreviousSingleTrackLevel != null) {
                    double courseDiffBetweenThisLevelPreviousSingleTrackLevelAndPreviousLevel = previousLevel
                            .getCourseAfter().getDifferenceTo(thisLevelPreviousSingleTrackLevel.getCourseAfter())
                            .getDegrees();
                    previousSingleTrackNode = previousNode
                            .getNextPointOfSail(courseDiffBetweenThisLevelPreviousSingleTrackLevelAndPreviousLevel);
                } else {
                    previousSingleTrackNode = previousNode;
                }
                probabilitiesSum += thisLevelCurrentSingleTrackLevel
                        .getProbabilityFromPreviousLevelNodeToThisLevelNode(previousSingleTrackNode, currentNode);
                probabilitiesCount++;
                double probability = probabilitiesSum / probabilitiesCount
                        * getNodeTransitionPenaltyFactor(previousNode, currentNode);
                setProbabilityFromPreviousLevelNodeToThisLevelNode(previousNode, currentNode, probability);
            }
        }
        normalizeNodeTransitions();
        calculationOfTransitionProbabilitiesNeeded = false;
    }

    private double getNodeTransitionPenaltyFactor(FineGrainedPointOfSail previousNode,
            FineGrainedPointOfSail currentNode) {
        if (getPreviousLevel() == null) {
            return 1;
        }
        double windCourseInDegreesOfPreviousNode = getPreviousLevel().getWindCourseInDegrees(previousNode);
        double windCourseInDegreesOfCurrentNode = getWindCourseInDegrees(currentNode);
        double absWindCourseShift = Math.abs(windCourseInDegreesOfPreviousNode - windCourseInDegreesOfCurrentNode);
        if (absWindCourseShift <= TRESHOLD_FOR_PENALTY_FREE_WIND_COURSE_SHIFT_IN_DEGREES) {
            return 1;
        }
        if (absWindCourseShift <= TRESHOLD_FOR_SMALL_PENALTY_WIND_COURSE_SHIFT_IN_DEGREES) {
            return 1 / (1 + (absWindCourseShift / TRESHOLD_FOR_SMALL_PENALTY_WIND_COURSE_SHIFT_IN_DEGREES));
        }
        return 1 / (4
                + Math.pow((absWindCourseShift - TRESHOLD_FOR_SMALL_PENALTY_WIND_COURSE_SHIFT_IN_DEGREES) / 5, 2));
    }

    public static ManeuverNodesLevelFactory<CrossTrackManeuverNodesLevel, SingleTrackManeuverNodesLevel> getFactory() {
        return new ManeuverNodesLevelFactory<CrossTrackManeuverNodesLevel, SingleTrackManeuverNodesLevel>() {

            @Override
            public CrossTrackManeuverNodesLevel createNewManeuverNodesLevel(
                    SingleTrackManeuverNodesLevel singleTrackManeuverNodesLevel) {
                return new CrossTrackManeuverNodesLevel(singleTrackManeuverNodesLevel);
            }
        };
    }

    public SingleTrackManeuverNodesLevel getSingleTrackManeuverNodesLevel() {
        return singleTrackManeuverNodesLevel;
    }

    @Override
    public BoatClass getBoatClass() {
        return singleTrackManeuverNodesLevel.getBoatClass();
    }

    @Override
    public boolean isManeuverBeginningClean() {
        return singleTrackManeuverNodesLevel.isManeuverBeginningClean();
    }

    @Override
    public boolean isManeuverEndClean() {
        return singleTrackManeuverNodesLevel.isManeuverEndClean();
    }

    @Override
    public void setTackProbabilityBonusToManeuver(double tackProbabilityBonus) {
        singleTrackManeuverNodesLevel.setTackProbabilityBonusToManeuver(tackProbabilityBonus);
        if (singleTrackManeuverNodesLevel.isCalculationOfTransitionProbabilitiesNeeded()) {
            this.calculationOfTransitionProbabilitiesNeeded = true;
            SingleTrackManeuverNodesLevel thisLevelPreviousSingleTrackLevel = singleTrackManeuverNodesLevel
                    .getPreviousLevel();
            if (thisLevelPreviousSingleTrackLevel != null) {
                CrossTrackManeuverNodesLevel matchedCrossTrackManeuverNodesLevel = getPreviousLevel();
                while (matchedCrossTrackManeuverNodesLevel != null
                        && thisLevelPreviousSingleTrackLevel != matchedCrossTrackManeuverNodesLevel
                                .getSingleTrackManeuverNodesLevel()) {
                    matchedCrossTrackManeuverNodesLevel = matchedCrossTrackManeuverNodesLevel.getPreviousLevel();
                }
                if (matchedCrossTrackManeuverNodesLevel != null) {
                    matchedCrossTrackManeuverNodesLevel.calculationOfTransitionProbabilitiesNeeded = true;
                }
            }
        }
    }

    @Override
    public boolean isCalculationOfTransitionProbabilitiesNeeded() {
        return calculationOfTransitionProbabilitiesNeeded;
    }

    @Override
    public CompleteManeuverCurveWithEstimationData getPreviousManeuverOfSameTrack() {
        return singleTrackManeuverNodesLevel.getPreviousManeuverOfSameTrack();
    }

    @Override
    public CompleteManeuverCurveWithEstimationData getNextManeuverOfSameTrack() {
        return singleTrackManeuverNodesLevel.getNextManeuverOfSameTrack();
    }

}

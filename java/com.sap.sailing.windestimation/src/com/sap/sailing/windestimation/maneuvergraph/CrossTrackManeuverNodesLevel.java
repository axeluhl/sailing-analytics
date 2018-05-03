package com.sap.sailing.windestimation.maneuvergraph;

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
                        double courseDiffBetweenThisLevelAndPreviousLevelNextSingleTrackLevel = this.getCourse()
                                .getDifferenceTo(previousLevelNextSingleTrackLevel.getCourse()).getDegrees();
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
                if (thisLevelPreviousSingleTrackLevel != null) {
                    double courseDiffBetweenThisLevelPreviousSingleTrackLevelAndPreviousLevel = thisLevelPreviousSingleTrackLevel
                            .getCourse().getDifferenceTo(previousLevel.getCourse()).getDegrees();
                    FineGrainedPointOfSail previousSingleTrackNode = previousNode
                            .getNextPointOfSail(courseDiffBetweenThisLevelPreviousSingleTrackLevelAndPreviousLevel);
                    probabilitiesSum += thisLevelCurrentSingleTrackLevel
                            .getProbabilityFromPreviousLevelNodeToThisLevelNode(previousSingleTrackNode, currentNode);
                    probabilitiesCount++;
                }
                double probability = probabilitiesSum / probabilitiesCount
                        * getNodeTransitionPenaltyFactor(previousLevel, previousNode, this, currentNode);
                this.nodeTransitions[currentNode.ordinal()].setProbabilitiesFromPreviousNodesLevel(previousNode,
                        probability);
            }
        }
        normalizeNodeTransitions();
    }

    private double getNodeTransitionPenaltyFactor(CrossTrackManeuverNodesLevel previousLevel,
            FineGrainedPointOfSail previousNode, CrossTrackManeuverNodesLevel crossTrackManeuverNodesLevel,
            FineGrainedPointOfSail currentNode) {
        double windCourseInDegreesOfPreviousNode = previousLevel.getWindCourseInDegrees(previousNode);
        double windCourseInDegreesOfCurrentNode = crossTrackManeuverNodesLevel.getWindCourseInDegrees(currentNode);
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

    private double getWindCourseInDegrees(FineGrainedPointOfSail node) {
        double windCourse = (getCourse().getDegrees() - node.getTwa() + 180) % 360;
        if (windCourse < 0) {
            windCourse += 360;
        }
        return windCourse;
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

}

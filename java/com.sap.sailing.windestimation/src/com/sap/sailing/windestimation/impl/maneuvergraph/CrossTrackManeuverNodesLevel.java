package com.sap.sailing.windestimation.impl.maneuvergraph;

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
    public void computeDistancesFromPreviousLevelToThisLevel() {
        for (FineGrainedPointOfSail previousNode : FineGrainedPointOfSail.values()) {
            CrossTrackManeuverNodesLevel previousLevel = getPreviousLevel();
            for (FineGrainedPointOfSail currentNode : FineGrainedPointOfSail.values()) {
                double distanceSum = 0;
                int distanceCount = 0;
                if (previousLevel != null) {
                    SingleTrackManeuverNodesLevel previousLevelNextSingleTrackLevel = previousLevel
                            .getSingleTrackManeuverNodesLevel().getNextLevel();
                    if (previousLevelNextSingleTrackLevel != null) {
                        double courseDiffBetweenThisLevelAndPreviousLevelNextSingleTrackLevel = this.getManeuver()
                                .getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getBearing()
                                .getDifferenceTo(previousLevelNextSingleTrackLevel.getManeuver()
                                        .getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getBearing())
                                .getDegrees();
                        FineGrainedPointOfSail nextSingleTrackNode = currentNode
                                .getNextPointOfSail(courseDiffBetweenThisLevelAndPreviousLevelNextSingleTrackLevel);
                        distanceSum += previousLevelNextSingleTrackLevel
                                .getDistanceFromPreviousLevelNodeToThisLevelNode(previousNode, nextSingleTrackNode);
                        distanceCount++;
                    }
                }
                SingleTrackManeuverNodesLevel thisLevelCurrentSingleTrackLevel = this
                        .getSingleTrackManeuverNodesLevel();
                SingleTrackManeuverNodesLevel thisLevelPreviousSingleTrackLevel = thisLevelCurrentSingleTrackLevel
                        .getPreviousLevel();
                if (thisLevelPreviousSingleTrackLevel != null) {
                    double courseDiffBetweenThisLevelPreviousSingleTrackLevelAndPreviousLevel = thisLevelPreviousSingleTrackLevel
                            .getManeuver().getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter()
                            .getBearing().getDifferenceTo(previousLevel.getManeuver()
                                    .getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getBearing())
                            .getDegrees();
                    FineGrainedPointOfSail previousSingleTrackNode = previousNode
                            .getNextPointOfSail(courseDiffBetweenThisLevelPreviousSingleTrackLevelAndPreviousLevel);
                    distanceSum += thisLevelCurrentSingleTrackLevel
                            .getDistanceFromPreviousLevelNodeToThisLevelNode(previousSingleTrackNode, currentNode);
                    distanceCount++;
                }
                double distance = distanceSum / distanceCount
                        * getNodeTransitionPenaltyFactor(previousLevel, previousNode, this, currentNode);
                this.nodeTransitions[currentNode.ordinal()].setDistancesFromPreviousNodesLevel(previousNode, distance);
            }
        }
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
        double windCourse = (getManeuver().getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getBearing()
                .getDegrees() - node.getTwa() + 180) % 360;
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

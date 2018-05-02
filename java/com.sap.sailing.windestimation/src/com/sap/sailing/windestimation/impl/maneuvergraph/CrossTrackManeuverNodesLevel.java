package com.sap.sailing.windestimation.impl.maneuvergraph;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class CrossTrackManeuverNodesLevel extends AbstractManeuverNodesLevel<CrossTrackManeuverNodesLevel>
        implements ManeuverNodesLevel<CrossTrackManeuverNodesLevel> {

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
        // TODO compare assumed wind direction from previous level and this level
        return 0;
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

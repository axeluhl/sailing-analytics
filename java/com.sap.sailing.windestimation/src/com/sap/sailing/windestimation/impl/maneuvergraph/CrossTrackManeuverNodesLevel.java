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
    public void computeDistances() {
        for (FineGrainedPointOfSail previousNode : FineGrainedPointOfSail.values()) {
            CrossTrackManeuverNodesLevel previousLevel = getPreviousLevel();
            for (FineGrainedPointOfSail currentNode : FineGrainedPointOfSail.values()) {
                double distanceSum = 0;
                int distanceCount = 0;
                if (previousLevel != null) {
                    SingleTrackManeuverNodesLevel previousLevelNextSingleTrackLevel = previousLevel
                            .getSingleTrackManeuverNodesLevel().getNextLevel();
                    if (previousLevelNextSingleTrackLevel != null) {
                        // TODO derive adequate previousLevelNode and thisLevelNode from assumed wind and represented
                        // course
                        // distanceSum +=
                        // previousLevelNextSingleTrackLevel.getDistanceFromPreviousLevelNodeToThisLevelNode(previousLevelNode,
                        // thisLevelNode)
                        distanceCount++;
                    }
                }
                SingleTrackManeuverNodesLevel thisLevelCurrentSingleTrackLevel = this
                        .getSingleTrackManeuverNodesLevel();
                if (thisLevelCurrentSingleTrackLevel.getPreviousLevel() != null) {
                    // TODO derive adequate previousLevelNode and thisLevelNode from assumed wind and represented course
                    // distanceSum +=
                    // thisLevelCurrentSingleTrackLevel.getDistanceFromPreviousLevelNodeToThisLevelNode(previousLevelNode,
                    // thisLevelNode);
                    distanceCount++;
                }
                // double distance *= distanceSum / distanceCount * getNodeTransitionPenaltyFactor(windBefore,
                // windAfter);
                // this.setDistanceFromPreviousLevelNodeToThisLevelNode(previousLevelNode, thisLevelNode)
            }
        }
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

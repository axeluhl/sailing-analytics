package com.sap.sailing.windestimation.maneuvergraph.impl;

import com.sap.sailing.windestimation.maneuvergraph.FineGrainedPointOfSail;
import com.sap.sailing.windestimation.maneuvergraph.ManeuverNodesLevel;
import com.sap.sailing.windestimation.maneuvergraph.ManeuverNodesLevelFactory;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class AdvancedCrossTrackManeuverNodesLevel
        extends AbstractCrossTrackManeuverNodesLevel<AdvancedCrossTrackManeuverNodesLevel>
        implements ManeuverNodesLevel<AdvancedCrossTrackManeuverNodesLevel> {

    public AdvancedCrossTrackManeuverNodesLevel(SingleTrackManeuverNodesLevel singleTrackManeuverNodesLevel) {
        super(singleTrackManeuverNodesLevel);
    }

    @Override
    public void computeProbabilitiesFromPreviousLevelToThisLevel() {
        for (FineGrainedPointOfSail previousNode : FineGrainedPointOfSail.values()) {
            AdvancedCrossTrackManeuverNodesLevel previousLevel = getPreviousLevel();
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

    public static ManeuverNodesLevelFactory<AdvancedCrossTrackManeuverNodesLevel, SingleTrackManeuverNodesLevel> getFactory() {
        return new ManeuverNodesLevelFactory<AdvancedCrossTrackManeuverNodesLevel, SingleTrackManeuverNodesLevel>() {

            @Override
            public AdvancedCrossTrackManeuverNodesLevel createNewManeuverNodesLevel(
                    SingleTrackManeuverNodesLevel singleTrackManeuverNodesLevel) {
                return new AdvancedCrossTrackManeuverNodesLevel(singleTrackManeuverNodesLevel);
            }
        };
    }

}

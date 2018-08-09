package com.sap.sailing.windestimation.maneuvergraph.impl;

import com.sap.sailing.windestimation.maneuvergraph.FineGrainedPointOfSail;
import com.sap.sailing.windestimation.maneuvergraph.ManeuverNodesLevel;
import com.sap.sailing.windestimation.maneuvergraph.ManeuverNodesLevelFactory;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class SimpleCrossTrackManeuverNodesLevel
        extends AbstractCrossTrackManeuverNodesLevel<SimpleCrossTrackManeuverNodesLevel>
        implements ManeuverNodesLevel<SimpleCrossTrackManeuverNodesLevel> {

    public SimpleCrossTrackManeuverNodesLevel(SingleTrackManeuverNodesLevel singleTrackManeuverNodesLevel) {
        super(singleTrackManeuverNodesLevel);
    }

    @Override
    public void computeProbabilitiesFromPreviousLevelToThisLevel() {
        for (FineGrainedPointOfSail previousNode : FineGrainedPointOfSail.values()) {
            for (FineGrainedPointOfSail currentNode : FineGrainedPointOfSail.values()) {
                double probability = this.getProbabilityFromPreviousLevelNodeToThisLevelNode(previousNode, currentNode)
                        * getNodeTransitionPenaltyFactor(previousNode, currentNode);
                setProbabilityFromPreviousLevelNodeToThisLevelNode(previousNode, currentNode, probability);
            }
        }
        normalizeNodeTransitions();
        calculationOfTransitionProbabilitiesNeeded = false;
    }

    public static ManeuverNodesLevelFactory<SimpleCrossTrackManeuverNodesLevel, SingleTrackManeuverNodesLevel> getFactory() {
        return new ManeuverNodesLevelFactory<SimpleCrossTrackManeuverNodesLevel, SingleTrackManeuverNodesLevel>() {

            @Override
            public SimpleCrossTrackManeuverNodesLevel createNewManeuverNodesLevel(
                    SingleTrackManeuverNodesLevel singleTrackManeuverNodesLevel) {
                return new SimpleCrossTrackManeuverNodesLevel(singleTrackManeuverNodesLevel);
            }
        };
    }

}

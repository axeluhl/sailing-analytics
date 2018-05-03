package com.sap.sailing.windestimation.maneuvergraph;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface ManeuverNodesLevel<SelfType extends ManeuverNodesLevel<SelfType>> {

    SelfType getNextLevel();

    SelfType getPreviousLevel();

    CompleteManeuverCurveWithEstimationData getManeuver();

    void computeProbabilitiesFromPreviousLevelToThisLevel();
    
    void computeBestPathsToThisLevel();
    
    void appendNextManeuverNodesLevel(SelfType nextManeuverNodesLevel);

    FineGrainedPointOfSail getBestPreviousNode(FineGrainedPointOfSail toNode);

    double getProbabilityOfBestPathToNodeFromStart(FineGrainedPointOfSail toNode);

    double getProbabilityFromPreviousLevelNodeToThisLevelNode(FineGrainedPointOfSail previousLevelNode,
            FineGrainedPointOfSail thisLevelNode);

}

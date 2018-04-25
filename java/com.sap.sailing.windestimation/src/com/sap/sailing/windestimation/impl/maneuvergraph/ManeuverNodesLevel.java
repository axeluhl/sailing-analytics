package com.sap.sailing.windestimation.impl.maneuvergraph;

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

    void computeDistancesFromPreviousLevelToThisLevel();
    
    void computeBestPathsToThisLevel();
    
    void appendNextManeuverNodesLevel(SelfType nextManeuverNodesLevel);

    FineGrainedPointOfSail getBestPreviousNode(FineGrainedPointOfSail toNode);

    double getBestDistanceToNodeFromStart(FineGrainedPointOfSail toNode);

    double getDistanceFromPreviousLevelNodeToThisLevelNode(FineGrainedPointOfSail previousLevelNode,
            FineGrainedPointOfSail thisLevelNode);

}

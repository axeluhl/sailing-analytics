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

    void computeDistances();
    
    void appendNextManeuverNodesLevel(SelfType nextManeuverNodesLevel);

    FineGrainedPointOfSail getBestPreviousNode(FineGrainedPointOfSail node);

    double[] getBestDistancesFromStart();

    double getDistanceToNodeFromStart(FineGrainedPointOfSail node);

}

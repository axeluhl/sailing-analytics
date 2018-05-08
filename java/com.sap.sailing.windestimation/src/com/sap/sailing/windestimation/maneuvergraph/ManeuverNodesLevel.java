package com.sap.sailing.windestimation.maneuvergraph;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.Bearing;
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

    void appendNextManeuverNodesLevel(SelfType nextManeuverNodesLevel);

    double getProbabilityFromPreviousLevelNodeToThisLevelNode(FineGrainedPointOfSail previousLevelNode,
            FineGrainedPointOfSail thisLevelNode);

    Bearing getCourse();

    default double getWindCourseInDegrees(FineGrainedPointOfSail node) {
        return getWindCourseInDegrees(node.getTwa());
    }

    double getWindCourseInDegrees(double twa);

    BoatClass getBoatClass();

}

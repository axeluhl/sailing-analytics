package com.sap.sailing.windestimation.maneuvergraph.impl.classifier;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface SingleManeuverClassifier {

    SingleManeuverClassificationResult classifyManeuver(CompleteManeuverCurveWithEstimationData maneuver, CompleteManeuverCurveWithEstimationData previousManeuver, CompleteManeuverCurveWithEstimationData nextManeuver);

}

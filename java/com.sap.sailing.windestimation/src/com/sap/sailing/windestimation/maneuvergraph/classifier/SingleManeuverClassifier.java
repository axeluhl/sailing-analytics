package com.sap.sailing.windestimation.maneuvergraph.classifier;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface SingleManeuverClassifier {

    SingleManeuverClassificationResult classifyManeuver(CompleteManeuverCurveWithEstimationData maneuver);

}

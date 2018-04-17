package com.sap.sailing.windestimation.impl;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;

public interface IManeuverSpeedRetriever {
    SpeedWithBearing getHighestSpeedWithinManeuverMainCurve(CompleteManeuverCurveWithEstimationData maneuverCurve);
}

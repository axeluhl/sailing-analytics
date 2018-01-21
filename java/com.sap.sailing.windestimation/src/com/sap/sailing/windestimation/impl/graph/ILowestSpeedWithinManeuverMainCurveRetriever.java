package com.sap.sailing.windestimation.impl.graph;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.tracking.Maneuver;

public interface ILowestSpeedWithinManeuverMainCurveRetriever {
    SpeedWithBearing getLowestSpeedWithinManeuverMainCurve(Maneuver maneuver);
}

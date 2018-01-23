package com.sap.sailing.windestimation.impl.maneuvergraph;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.tracking.Maneuver;

public interface IManeuverSpeedRetriever {
    SpeedWithBearing getLowestSpeedWithinManeuverMainCurve(Maneuver maneuver);

    SpeedWithBearing getHighestSpeedWithinManeuverMainCurve(Maneuver maneuver);
}

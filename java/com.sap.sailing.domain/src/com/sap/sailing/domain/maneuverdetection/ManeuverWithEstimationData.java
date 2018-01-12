package com.sap.sailing.domain.maneuverdetection;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sse.common.Duration;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface ManeuverWithEstimationData {
    Maneuver getManeuver();

    Wind getWind();

    SpeedWithBearing getHighestSpeedWithinMainCurve();

    SpeedWithBearing getLowestSpeedWithinMainCurve();

    SpeedWithBearing getAverageSpeedWithBearingBefore();

    Duration getDurationFromPreviousManeuverEndToManeuverStart();

    SpeedWithBearing getAverageSpeedWithBearingAfter();

    Duration getDurationFromManeuverEndToNextManeuverStart();
}

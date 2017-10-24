package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sse.common.TimePoint;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;

public interface Maneuver extends GPSFix {
    ManeuverType getType();

    @Dimension(messageKey = "Tack", ordinal = 13)
    Tack getNewTack();
    
    Distance getManeuverLoss();

    /**
     * Gets the computed time point of the corresponding maneuver. The time point refers to a position within
     * maneuver, where the highest course change has been recorded.
     * 
     * @return The computed maneuver time point
     */
    TimePoint getTimePoint();
    
    /**
     * Gets the computed time point of maneuver start.
     * 
     * @return The time point of maneuver start
     */
    TimePoint getTimePointBefore();

    /**
     * Gets the computed time point of maneuver end.
     * 
     * @return The time point of maneuver end
     */
    TimePoint getTimePointAfter();

    /**
     * Gets the speed with bearing at maneuver start.
     * 
     * @return The speed with bearing at maneuver start
     */
    SpeedWithBearing getSpeedWithBearingBefore();

    /**
     * Gets the speed with bearing at maneuver end.
     * 
     * @return The speed with bearing at maneuver end
     */
    SpeedWithBearing getSpeedWithBearingAfter();

    /**
     * Gets the total course change performed within maneuver in degrees. The port side course changes are negative.
     * 
     * @return The total course change in degrees
     */
    @Statistic(messageKey = "DirectionChange", resultDecimals = 2, ordinal = 2)
    double getDirectionChangeInDegrees();

    TimePoint getTimePointBeforeMainCurve();

    TimePoint getTimePointAfterMainCurve();
    
    @Statistic(messageKey = "DirectionChangeWithinMainCurve", resultDecimals = 2, ordinal = 3)
    double getDirectionChangeWithinMainCurveInDegrees();

}

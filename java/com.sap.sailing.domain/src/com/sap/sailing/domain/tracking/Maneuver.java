package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sse.common.TimePoint;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;

/**
 * Represents a maneuver detected within a competitor track. 
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface Maneuver extends GPSFix {
    ManeuverType getType();

    @Dimension(messageKey = "Tack", ordinal = 13)
    Tack getNewTack();
    
    /**
     * Gets the the maneuver loss of this maneuver which is the distance projected onto the average course between entering and exiting the
     * maneuver that the boat lost compared to not having maneuvered.
     */
    Distance getManeuverLoss();

    /**
     * Gets the time point of the corresponding maneuver. The time point refers to a position within
     * the main curve of maneuver with the highest course change recorded toward the direction of maneuver.
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
     * Gets the total course change performed within maneuver between {@link #getTimePointBefore()} and
     * {@link #getTimePointAfter()} in degrees. The port side course changes are negative. The value may exceed 360
     * degrees if the performed maneuver is a penalty circle.
     * 
     * @return The total course change within the whole maneuver in degrees
     */
    @Statistic(messageKey = "DirectionChange", resultDecimals = 2, ordinal = 2)
    double getDirectionChangeInDegrees();

    TimePoint getTimePointBeforeMainCurve();

    TimePoint getTimePointAfterMainCurve();
    
    /**
     * Gets the total course change performed within the main curve of maneuver between
     * {@link #getTimePointBeforeMainCurve()} and {@link #getTimePointAfterMainCurve()} in degrees. The port side course
     * changes are negative. The value may exceed 360 degrees if the performed maneuver is a penalty circle.
     * 
     * @return The total course change with the main curve in degrees
     */
    @Statistic(messageKey = "DirectionChangeWithinMainCurve", resultDecimals = 2, ordinal = 3)
    double getDirectionChangeWithinMainCurveInDegrees();

}

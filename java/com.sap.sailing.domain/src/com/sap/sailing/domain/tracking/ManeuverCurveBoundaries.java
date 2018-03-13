package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsDurationImpl;

/**
 * Represents the entering and exiting time point of a maneuver curve with corresponding speeds and courses.
 * Additionally, the total course change of the curve which is calculated throughout the iteration of bearing steps of
 * the maneuver curve is also available.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface ManeuverCurveBoundaries {

    /**
     * Gets the computed time point of curve start.
     * 
     * @return The time point of curve start
     */
    TimePoint getTimePointBefore();

    /**
     * Gets the computed time point of curve end.
     * 
     * @return The time point of curve end
     */
    TimePoint getTimePointAfter();

    /**
     * Gets the speed with bearing at curve start.
     * 
     * @return The speed with bearing at curve start
     */
    SpeedWithBearing getSpeedWithBearingBefore();

    /**
     * Gets the speed with bearing at curve end.
     * 
     * @return The speed with bearing at curve end
     */
    SpeedWithBearing getSpeedWithBearingAfter();

    /**
     * Gets the total course change performed within the curve in degrees. The port side course changes are negative.
     * 
     * @return The total course change in degrees
     */
    double getDirectionChangeInDegrees();

    default Duration getDuration(){
        if(getTimePointAfter() == null && getTimePointBefore() == null){
            return null;
        }
        long differenceInMs = getTimePointAfter().asMillis()-getTimePointBefore().asMillis();
        return new MillisecondsDurationImpl(differenceInMs);
    }

    /**
     * Gets the lowest speed sailed within the maneuver curve.
     * 
     * @return The lowest speed within maneuver curve
     */
    Speed getLowestSpeed();

}

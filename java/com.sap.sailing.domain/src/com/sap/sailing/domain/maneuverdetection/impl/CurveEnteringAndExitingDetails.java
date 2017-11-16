package com.sap.sailing.domain.maneuverdetection.impl;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sse.common.TimePoint;

/**
 * Represents the entering and exiting time point of the curve with corresponding speeds and bearings.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
class CurveEnteringAndExitingDetails {
    private final TimePoint timePointBefore;
    private final TimePoint timePointAfter;
    private final SpeedWithBearing speedWithBearingBefore;
    private final SpeedWithBearing speedWithBearingAfter;

    public CurveEnteringAndExitingDetails(TimePoint timePointBefore, TimePoint timePointAfter,
            SpeedWithBearing speedWithBearingBefore, SpeedWithBearing speedWithBearingAfter) {
        this.timePointBefore = timePointBefore;
        this.timePointAfter = timePointAfter;
        this.speedWithBearingBefore = speedWithBearingBefore;
        this.speedWithBearingAfter = speedWithBearingAfter;
    }

    /**
     * Gets the computed time point of curve start.
     * 
     * @return The time point of curve start
     */
    public TimePoint getTimePointBefore() {
        return timePointBefore;
    }

    /**
     * Gets the computed time point of curve end.
     * 
     * @return The time point of curve end
     */
    public TimePoint getTimePointAfter() {
        return timePointAfter;
    }

    /**
     * Gets the speed with bearing at curve start.
     * 
     * @return The speed with bearing at curve start
     */
    public SpeedWithBearing getSpeedWithBearingBefore() {
        return speedWithBearingBefore;
    }

    /**
     * Gets the speed with bearing at curve end.
     * 
     * @return The speed with bearing at curve end
     */
    public SpeedWithBearing getSpeedWithBearingAfter() {
        return speedWithBearingAfter;
    }
}
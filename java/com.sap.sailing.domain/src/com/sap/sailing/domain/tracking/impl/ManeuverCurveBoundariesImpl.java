package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.tracking.ManeuverCurveBoundaries;
import com.sap.sse.common.TimePoint;

/**
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverCurveBoundariesImpl implements ManeuverCurveBoundaries {
    private final TimePoint timePointBefore;
    private final TimePoint timePointAfter;
    private final SpeedWithBearing speedWithBearingBefore;
    private final SpeedWithBearing speedWithBearingAfter;
    private final double directionChangeInDegrees;

    public ManeuverCurveBoundariesImpl(TimePoint timePointBefore, TimePoint timePointAfter,
            SpeedWithBearing speedWithBearingBefore, SpeedWithBearing speedWithBearingAfter,
            double directionChangeInDegrees) {
        this.timePointBefore = timePointBefore;
        this.timePointAfter = timePointAfter;
        this.speedWithBearingBefore = speedWithBearingBefore;
        this.speedWithBearingAfter = speedWithBearingAfter;
        this.directionChangeInDegrees = directionChangeInDegrees;
    }

    @Override
    public TimePoint getTimePointBefore() {
        return timePointBefore;
    }

    @Override
    public TimePoint getTimePointAfter() {
        return timePointAfter;
    }

    @Override
    public SpeedWithBearing getSpeedWithBearingBefore() {
        return speedWithBearingBefore;
    }

    @Override
    public SpeedWithBearing getSpeedWithBearingAfter() {
        return speedWithBearingAfter;
    }

    @Override
    public double getDirectionChangeInDegrees() {
        return directionChangeInDegrees;
    }

    @Override
    public String toString() {
        return "Starting at time point " + timePointBefore + ", ending at time point " + timePointAfter
                + ". Speed before curve " + speedWithBearingBefore + " speed after curve " + speedWithBearingAfter
                + ". Course changed by " + directionChangeInDegrees + "deg.";
    }
}
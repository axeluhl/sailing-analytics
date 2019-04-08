package com.sap.sailing.domain.maneuverdetection.impl;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.maneuverdetection.ManeuverCurveBoundariesWithDetailedManeuverLoss;
import com.sap.sailing.domain.tracking.impl.ManeuverCurveBoundariesImpl;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverCurveBoundariesWithDetailedManeuverLossImpl extends ManeuverCurveBoundariesImpl
        implements ManeuverCurveBoundariesWithDetailedManeuverLoss {

    private final Distance distanceSailedWithinManeuver;
    private final Distance distanceSailedWithinManeuverTowardMiddleAngleProjection;
    private final Distance distanceSailedIfNotManeuvering;
    private final Distance distanceSailedTowardMiddleAngleProjectionIfNotManeuvering;

    public ManeuverCurveBoundariesWithDetailedManeuverLossImpl(TimePoint timePointBefore, TimePoint timePointAfter,
            SpeedWithBearing speedWithBearingBefore, SpeedWithBearing speedWithBearingAfter,
            double directionChangeInDegrees, Speed lowestSpeed, Distance distanceSailedWithinManeuver,
            Distance distanceSailedWithinManeuverTowardMiddleAngleProjection, Distance distanceSailedIfNotManeuvering,
            Distance distanceSailedTowardMiddleAngleProjectionIfNotManeuvering) {
        super(timePointBefore, timePointAfter, speedWithBearingBefore, speedWithBearingAfter, directionChangeInDegrees,
                lowestSpeed);
        this.distanceSailedWithinManeuver = distanceSailedWithinManeuver;
        this.distanceSailedWithinManeuverTowardMiddleAngleProjection = distanceSailedWithinManeuverTowardMiddleAngleProjection;
        this.distanceSailedIfNotManeuvering = distanceSailedIfNotManeuvering;
        this.distanceSailedTowardMiddleAngleProjectionIfNotManeuvering = distanceSailedTowardMiddleAngleProjectionIfNotManeuvering;
    }

    @Override
    public Distance getDistanceSailedWithinManeuver() {
        return distanceSailedWithinManeuver;
    }

    @Override
    public Distance getDistanceSailedWithinManeuverTowardMiddleAngleProjection() {
        return distanceSailedWithinManeuverTowardMiddleAngleProjection;
    }

    @Override
    public Distance getDistanceSailedIfNotManeuvering() {
        return distanceSailedIfNotManeuvering;
    }

    @Override
    public Distance getDistanceSailedTowardMiddleAngleProjectionIfNotManeuvering() {
        return distanceSailedTowardMiddleAngleProjectionIfNotManeuvering;
    }

}

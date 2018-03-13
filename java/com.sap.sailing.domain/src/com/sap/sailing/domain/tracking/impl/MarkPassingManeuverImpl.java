package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.tracking.ManeuverCurveBoundaries;
import com.sap.sailing.domain.tracking.MarkPassingManeuver;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public class MarkPassingManeuverImpl extends ManeuverWithStableSpeedAndCourseBoundariesImpl
        implements MarkPassingManeuver {
    private static final long serialVersionUID = 8935348908557191614L;
    private final Waypoint waypointPassed;
    private final NauticalSide side;

    public MarkPassingManeuverImpl(ManeuverType type, Tack newTack, Position position, Distance maneuverLoss,
            TimePoint timePoint, ManeuverCurveBoundaries mainCurveBoundaries,
            ManeuverCurveBoundaries maneuverCurveWithStableSpeedAndCourseBoundaries,
            double maxAngularVelocityInDegreesPerSecond, Waypoint waypointPassed, NauticalSide side, Duration duration) {
        super(type, newTack, position, maneuverLoss, timePoint, mainCurveBoundaries,
                maneuverCurveWithStableSpeedAndCourseBoundaries, maxAngularVelocityInDegreesPerSecond, duration);
        this.waypointPassed = waypointPassed;
        this.side = side;
    }

    @Override
    public Waypoint getWaypointPassed() {
        return waypointPassed;
    }
    
    @Override
    public NauticalSide getSide() {
        return side;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(super.toString());
        result.append(", passed waypoint " + getWaypointPassed() + " to " + getSide().name());
        return result.toString();
    }

}

package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.tracking.Maneuver;

public class ManeuverImpl implements Maneuver {
    private final Type type;
    private final Position position;
    private final TimePoint timePoint;
    private final SpeedWithBearing speedWithBearingBefore;
    private final SpeedWithBearing speedWithBearingAfter;
    private final double directionChangeInDegrees;

    public ManeuverImpl(Type type, Position position, TimePoint timePoint, SpeedWithBearing speedWithBearingBefore,
            SpeedWithBearing speedWithBearingAfter, double directionChangeInDegrees) {
        super();
        this.type = type;
        this.position = position;
        this.timePoint = timePoint;
        this.speedWithBearingBefore = speedWithBearingBefore;
        this.speedWithBearingAfter = speedWithBearingAfter;
        this.directionChangeInDegrees = directionChangeInDegrees;
    }
    
    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public TimePoint getTimePoint() {
        return timePoint;
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

}

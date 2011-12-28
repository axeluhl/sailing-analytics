package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.Tack;
import com.sap.sailing.domain.base.TimePoint;
import com.sap.sailing.domain.tracking.Maneuver;

public class ManeuverImpl extends AbstractGPSFixImpl implements Maneuver {
    private final Type type;
    private final Tack newTack;
    private final Position position;
    private final TimePoint timePoint;
    private final SpeedWithBearing speedWithBearingBefore;
    private final SpeedWithBearing speedWithBearingAfter;
    private final double directionChangeInDegrees;

    public ManeuverImpl(Type type, Tack newTack, Position position, TimePoint timePoint, SpeedWithBearing speedWithBearingBefore,
            SpeedWithBearing speedWithBearingAfter, double directionChangeInDegrees) {
        super();
        this.type = type;
        this.newTack = newTack;
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

    @Override
    public Tack getNewTack() {
        return newTack;
    }

    @Override
    public String toString() {
        return super.toString() + " " + type + " on new tack " + newTack + " on position " + position
                + " at time point " + timePoint + ". " + "Speed before maneuver " + speedWithBearingBefore
                + " speed after maneuver " + speedWithBearingAfter + ". The maneuver changed the course by "
                + directionChangeInDegrees + ".";
    }
}

package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.tracking.impl.AbstractGPSFixImpl;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sse.common.TimePoint;

/**
 * @author Axel Uhl (d043530)
 *
 */
public class ManeuverImpl extends AbstractGPSFixImpl implements Maneuver {
    private static final long serialVersionUID = -5317959066507472580L;
    private final ManeuverType type;
    private final Tack newTack;
    private final Position position;
    private final TimePoint timePoint;
    private final TimePoint timePointBefore;
    private final TimePoint timePointAfter;
    private final SpeedWithBearing speedWithBearingBefore;
    private final SpeedWithBearing speedWithBearingAfter;
    private final double directionChangeInDegrees;
    private final Distance maneuverLoss;
    private final TimePoint timePointBeforeMainCurve;
    private final TimePoint timePointAfterMainCurve;
    private final double directionChangeWithinMainCurveInDegrees;
    private final double maxAngularVelocityInDegreesPerSecond;

    public ManeuverImpl(ManeuverType type, Tack newTack, Position position, Distance maneuverLoss, TimePoint timePoint,
            TimePoint timePointBefore, TimePoint timePointAfter, SpeedWithBearing speedWithBearingBefore,
            SpeedWithBearing speedWithBearingAfter, double directionChangeInDegrees, TimePoint timePointBeforeMainCurve,
            TimePoint timePointAfterMainCurve, double directionChangeWithinMainCurveInDegrees, double maxAngularVelocityInDegreesPerSecond) {
        super();
        this.type = type;
        this.newTack = newTack;
        this.position = position;
        this.maneuverLoss = maneuverLoss;
        this.timePoint = timePoint;
        this.timePointBefore = timePointBefore;
        this.timePointAfter = timePointAfter;
        this.speedWithBearingBefore = speedWithBearingBefore;
        this.speedWithBearingAfter = speedWithBearingAfter;
        this.directionChangeInDegrees = directionChangeInDegrees;
        this.timePointBeforeMainCurve = timePointBeforeMainCurve;
        this.timePointAfterMainCurve = timePointAfterMainCurve;
        this.directionChangeWithinMainCurveInDegrees = directionChangeWithinMainCurveInDegrees;
        this.maxAngularVelocityInDegreesPerSecond = maxAngularVelocityInDegreesPerSecond;
    }

    @Override
    public ManeuverType getType() {
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
    public Distance getManeuverLoss() {
        return maneuverLoss;
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
    public String toString() {
        return super.toString() + " " + type + " on new tack " + newTack + " on position " + position
                + " at time point " + timePoint + ", starting at time point " + timePointBefore + ", ending at time point " + timePointAfter + ". Speed before maneuver " + speedWithBearingBefore
                + " speed after maneuver " + speedWithBearingAfter + ". The maneuver changed the course by "
                + directionChangeInDegrees + "deg."
                + (getManeuverLoss() == null ? "" : " Lost approximately " + getManeuverLoss());
    }

    @Override
    public TimePoint getTimePointBeforeMainCurve() {
        return timePointBeforeMainCurve;
    }

    @Override
    public TimePoint getTimePointAfterMainCurve() {
        return timePointAfterMainCurve;
    }

    @Override
    public double getDirectionChangeWithinMainCurveInDegrees() {
        return directionChangeWithinMainCurveInDegrees;
    }
    
    @Override
    public double getMaxAngularVelocityInDegreesPerSecond() {
        return maxAngularVelocityInDegreesPerSecond;
    }

}

package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
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
    private final SpeedWithBearing speedWithBearingBefore;
    private final SpeedWithBearing speedWithBearingAfter;
    private final double directionChangeInDegrees;
    private final Distance maneuverLoss;

    public ManeuverImpl(ManeuverType type, Tack newTack, Position position, TimePoint timePoint, SpeedWithBearing speedWithBearingBefore,
            SpeedWithBearing speedWithBearingAfter, double directionChangeInDegrees, Distance maneuverLoss) {
        super();
        this.type = type;
        this.newTack = newTack;
        this.position = position;
        this.timePoint = timePoint;
        this.speedWithBearingBefore = speedWithBearingBefore;
        this.speedWithBearingAfter = speedWithBearingAfter;
        this.directionChangeInDegrees = directionChangeInDegrees;
        this.maneuverLoss = maneuverLoss;
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
    public String toString() {
        return super.toString() + " " + type + " on new tack " + newTack + " on position " + position
                + " at time point " + timePoint + ". " + "Speed before maneuver " + speedWithBearingBefore
                + " speed after maneuver " + speedWithBearingAfter + ". The maneuver changed the course by "
                + directionChangeInDegrees + "deg." + (getManeuverLoss() == null ? "" : " Lost approximately "+getManeuverLoss());
    }
}

package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.tracking.impl.AbstractGPSFixImpl;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.ManeuverCurveEnteringAndExitingDetails;
import com.sap.sse.common.TimePoint;

/**
 * @author Axel Uhl (d043530)
 *
 */
public abstract class ManeuverImpl extends AbstractGPSFixImpl implements Maneuver {
    private static final long serialVersionUID = -5317959066507472580L;
    private final ManeuverType type;
    private final Tack newTack;
    private final Position position;
    private final TimePoint timePoint;
    private final Distance maneuverLoss;
    private final double maxAngularVelocityInDegreesPerSecond;
    private final ManeuverCurveEnteringAndExitingDetails mainCurveEnteringAndExitingDetails;
    private final ManeuverCurveEnteringAndExitingDetails maneuverCurveWithStableSpeedAndCourseBeforeAndAfterEnteringAndExistingDetails;

    public ManeuverImpl(ManeuverType type, Tack newTack, Position position, Distance maneuverLoss, TimePoint timePoint,
            ManeuverCurveEnteringAndExitingDetails mainCurveEnteringAndExistingDetails,
            ManeuverCurveEnteringAndExitingDetails maneuverCurveWithStableSpeedAndCourseBeforeAndAfterEnteringAndExistingDetails,
            double maxAngularVelocityInDegreesPerSecond) {
        this.type = type;
        this.newTack = newTack;
        this.position = position;
        this.maneuverLoss = maneuverLoss;
        this.timePoint = timePoint;
        this.mainCurveEnteringAndExitingDetails = mainCurveEnteringAndExistingDetails;
        this.maneuverCurveWithStableSpeedAndCourseBeforeAndAfterEnteringAndExistingDetails = maneuverCurveWithStableSpeedAndCourseBeforeAndAfterEnteringAndExistingDetails;
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
    public ManeuverCurveEnteringAndExitingDetails getMainCurveEnteringAndExitingDetails() {
        return mainCurveEnteringAndExitingDetails;
    }

    @Override
    public ManeuverCurveEnteringAndExitingDetails getManeuverCurveWithStableSpeedAndCourseBeforeAndAfterEnteringAndExistingDetails() {
        return maneuverCurveWithStableSpeedAndCourseBeforeAndAfterEnteringAndExistingDetails;
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
    public double getDirectionChangeInDegrees() {
        return getManeuverEnteringAndExistingDetails().getDirectionChangeInDegrees();
    }
    
    @Override
    public SpeedWithBearing getSpeedWithBearingBefore() {
        return getManeuverEnteringAndExistingDetails().getSpeedWithBearingBefore();
    }
    
    @Override
    public SpeedWithBearing getSpeedWithBearingAfter() {
        return getManeuverEnteringAndExistingDetails().getSpeedWithBearingAfter();
    }

    @Override
    public String toString() {
        return super.toString() + " " + type + " on new tack " + newTack + " on position " + position
                + " at time point " + timePoint + ", " + getManeuverEnteringAndExistingDetails() + ", max. angular velocity: " + maxAngularVelocityInDegreesPerSecond
                + (getManeuverLoss() == null ? "" : " Lost approximately " + getManeuverLoss());
    }

    @Override
    public double getMaxAngularVelocityInDegreesPerSecond() {
        return maxAngularVelocityInDegreesPerSecond;
    }

}

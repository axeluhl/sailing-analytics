package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.ManeuverCurveEnteringAndExitingDetails;
import com.sap.sse.common.TimePoint;

/**
 * Represents maneuvers which start and end is unknown, e.g. due to not available wind information, and thus not
 * available maneuver type.
 * 
 * @author Vladislav Chumak (D069712)
 * @see Maneuver
 */
public class ManeuverWithUnknownBoundariesImpl extends ManeuverImpl {

    private static final long serialVersionUID = 5831188137884083419L;

    public ManeuverWithUnknownBoundariesImpl(ManeuverType type, Tack newTack, Position position, Distance maneuverLoss,
            TimePoint timePoint, ManeuverCurveEnteringAndExitingDetails mainCurveEnteringAndExistingDetails,
            ManeuverCurveEnteringAndExitingDetails maneuverCurveWithStableSpeedAndCourseBeforeAndAfterEnteringAndExistingDetails,
            double maxAngularVelocityInDegreesPerSecond) {
        super(type, newTack, position, maneuverLoss, timePoint, mainCurveEnteringAndExistingDetails,
                maneuverCurveWithStableSpeedAndCourseBeforeAndAfterEnteringAndExistingDetails,
                maxAngularVelocityInDegreesPerSecond);
    }

    @Override
    public ManeuverCurveEnteringAndExitingDetails getManeuverEnteringAndExistingDetails() {
        throw new IllegalStateException("The boundaries of maneuver are unknown");
    }

}

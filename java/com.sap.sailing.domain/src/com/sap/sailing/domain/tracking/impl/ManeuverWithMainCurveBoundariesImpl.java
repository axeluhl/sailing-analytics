package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.ManeuverCurveBoundaries;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

/**
 * Represents maneuvers which start and end encompasses solely the main curve.
 * 
 * @author Vladislav Chumak (D069712)
 * @see Maneuver
 */
public class ManeuverWithMainCurveBoundariesImpl extends ManeuverImpl {

    private static final long serialVersionUID = 5831188137884083419L;

    public ManeuverWithMainCurveBoundariesImpl(ManeuverType type, Tack newTack, Position position,
            Distance maneuverLoss, TimePoint timePoint, ManeuverCurveBoundaries mainCurveBoundaries,
            ManeuverCurveBoundaries maneuverCurveWithStableSpeedAndCourseBoundaries,
            double maxAngularVelocityInDegreesPerSecond, Duration duration, SpeedWithBearing minSpeed) {
        super(type, newTack, position, maneuverLoss, timePoint, mainCurveBoundaries,
                maneuverCurveWithStableSpeedAndCourseBoundaries, maxAngularVelocityInDegreesPerSecond, duration, minSpeed);
    }

    @Override
    public ManeuverCurveBoundaries getManeuverBoundaries() {
        return getMainCurveBoundaries();
    }

}

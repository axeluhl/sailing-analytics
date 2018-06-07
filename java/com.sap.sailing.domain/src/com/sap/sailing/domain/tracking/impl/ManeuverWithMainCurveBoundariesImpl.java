package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.ManeuverCurveBoundaries;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sse.common.Distance;
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
            double maxTurningRateInDegreesPerSecond, MarkPassing markPassing) {
        super(type, newTack, position, maneuverLoss, timePoint, mainCurveBoundaries,
                maneuverCurveWithStableSpeedAndCourseBoundaries, maxTurningRateInDegreesPerSecond, markPassing);
    }

    @Override
    public ManeuverCurveBoundaries getManeuverBoundaries() {
        return getMainCurveBoundaries();
    }

}

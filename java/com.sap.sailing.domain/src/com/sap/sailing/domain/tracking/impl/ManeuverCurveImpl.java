package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.maneuverdetection.impl.ManeuverCurveDetails;
import com.sap.sailing.domain.maneuverdetection.impl.ManeuverCurveDetailsWithBearingSteps;
import com.sap.sailing.domain.tracking.ManeuverCurve;
import com.sap.sailing.domain.tracking.MarkPassing;

/**
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverCurveImpl implements ManeuverCurve {

    private final ManeuverCurveDetailsWithBearingSteps mainCurveBoundaries;
    private final ManeuverCurveDetails maneuverCurveWithStableSpeedAndCourseBoundaries;
    private final MarkPassing markPassing;

    public ManeuverCurveImpl(ManeuverCurveDetailsWithBearingSteps mainCurveBoundaries,
            ManeuverCurveDetails maneuverCurveWithStableSpeedAndCourseBoundaries, MarkPassing markPassing) {
        this.mainCurveBoundaries = mainCurveBoundaries;
        this.maneuverCurveWithStableSpeedAndCourseBoundaries = maneuverCurveWithStableSpeedAndCourseBoundaries;
        this.markPassing = markPassing;
    }

    @Override
    public ManeuverCurveDetailsWithBearingSteps getMainCurveBoundaries() {
        return mainCurveBoundaries;
    }

    @Override
    public ManeuverCurveDetails getManeuverCurveWithStableSpeedAndCourseBoundaries() {
        return maneuverCurveWithStableSpeedAndCourseBoundaries;
    }

    public MarkPassing getMarkPassing() {
        return markPassing;
    }

    public boolean isMarkPassing() {
        return markPassing != null;
    }

}

package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.maneuverdetection.impl.ManeuverMainCurveDetailsWithBearingSteps;
import com.sap.sailing.domain.tracking.CompleteManeuverCurve;
import com.sap.sailing.domain.tracking.ManeuverCurveBoundaries;
import com.sap.sailing.domain.tracking.MarkPassing;

/**
 * @author Vladislav Chumak (D069712)
 *
 */
public class CompleteManeuverCurveImpl implements CompleteManeuverCurve {

    private final ManeuverMainCurveDetailsWithBearingSteps mainCurveBoundaries;
    private final ManeuverCurveBoundaries maneuverCurveWithStableSpeedAndCourseBoundaries;
    private final MarkPassing markPassing;

    public CompleteManeuverCurveImpl(ManeuverMainCurveDetailsWithBearingSteps mainCurveBoundaries,
            ManeuverCurveBoundaries maneuverCurveWithStableSpeedAndCourseBoundaries, MarkPassing markPassing) {
        this.mainCurveBoundaries = mainCurveBoundaries;
        this.maneuverCurveWithStableSpeedAndCourseBoundaries = maneuverCurveWithStableSpeedAndCourseBoundaries;
        this.markPassing = markPassing;
    }

    @Override
    public ManeuverMainCurveDetailsWithBearingSteps getMainCurveBoundaries() {
        return mainCurveBoundaries;
    }

    @Override
    public ManeuverCurveBoundaries getManeuverCurveWithStableSpeedAndCourseBoundaries() {
        return maneuverCurveWithStableSpeedAndCourseBoundaries;
    }

    @Override
    public MarkPassing getMarkPassing() {
        return markPassing;
    }

    @Override
    public boolean isMarkPassing() {
        return markPassing != null;
    }

}

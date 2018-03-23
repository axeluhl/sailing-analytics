package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.maneuverdetection.impl.ManeuverCurveDetails;
import com.sap.sailing.domain.maneuverdetection.impl.ManeuverCurveDetailsWithBearingSteps;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface ManeuverCurve {

    /**
     * Gets time points and speeds with bearings of main curve beginning and end.
     * 
     * @return Entering and exiting details of maneuver main curve
     * @see ManeuverCurve
     */
    ManeuverCurveDetailsWithBearingSteps getMainCurveBoundaries();

    /**
     * Gets time points and speeds with bearings before and after the maneuver, such that the speed and course before
     * and after the maneuver are considered as stable.
     * 
     * @return Entering and exiting details of maneuver section, with stable speed and bearing before and after that
     *         section
     * @see ManeuverCurve
     */
    ManeuverCurveDetails getManeuverCurveWithStableSpeedAndCourseBoundaries();

    MarkPassing getMarkPassing();

    boolean isMarkPassing();

}

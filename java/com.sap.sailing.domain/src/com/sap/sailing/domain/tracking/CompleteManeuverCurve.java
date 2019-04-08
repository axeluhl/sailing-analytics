package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.maneuverdetection.impl.ManeuverMainCurveDetailsWithBearingSteps;

/**
 * Represents a segment within a GPS-track where a maneuver is performed. In contrast to {@link Maneuver}, the
 * boundaries of the curve are not split by maneuver type dependent logic, e.g. by cutting of the first 360 degrees of
 * penalty circles and etc.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface CompleteManeuverCurve {

    /**
     * Gets time points and speeds with bearings of main curve beginning and end.
     * 
     * @return Entering and exiting details of maneuver main curve
     * @see CompleteManeuverCurve
     */
    ManeuverMainCurveDetailsWithBearingSteps getMainCurveBoundaries();

    /**
     * Gets time points and speeds with bearings before and after the maneuver, such that the speed and course before
     * and after the maneuver are considered as stable.
     * 
     * @return Entering and exiting details of maneuver section, with stable speed and bearing before and after that
     *         section
     * @see CompleteManeuverCurve
     */
    ManeuverCurveBoundaries getManeuverCurveWithStableSpeedAndCourseBoundaries();

    /**
     * Gets the mark passing which is contained within maneuver curve. In case if no mark was passed, {@code null} is
     * returned.
     */
    MarkPassing getMarkPassing();

    /**
     * Determines whether a mark was crossed within the maneuver curve.
     */
    boolean isMarkPassing();

}

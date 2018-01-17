package com.sap.sailing.domain.maneuverdetection;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sse.common.Duration;

/**
 * Contains maneuver, as well as additional maneuver information which is regarded as relevant for maneuver
 * classification algorithms, such as the wind estimation.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface ManeuverWithEstimationData {

    /**
     * Gets the represented maneuver.
     */
    Maneuver getManeuver();

    /**
     * Gets the wind measured at the {@link Maneuver#getTimePoint(). Can be {@code null} in cases, when no wind
     * information is available.
     */
    Wind getWind();

    /**
     * Gets the highest speed measured within the main curve with corresponding course.
     */
    SpeedWithBearing getHighestSpeedWithinMainCurve();

    /**
     * Gets the lowest speed measured within the main curve with corresponding course.
     */
    SpeedWithBearing getLowestSpeedWithinMainCurve();

    /**
     * Gets the average speed and course measured from the end of the previous maneuver until the start of this
     * maneuver. The mentioned maneuver start and end refer to
     * {@link Maneuver#getManeuverCurveWithStableSpeedAndCourseBoundaries(). If there are no previous maneuvers, or the
     * previous maneuver end is not before the start of this maneuver with a distance of at least one second, the result
     * will be {@null}.
     */
    SpeedWithBearing getAverageSpeedWithBearingBefore();

    /**
     * Gets the duration from the end of the previous maneuver until the start of this maneuver. The mentioned maneuver
     * start and end refer to {@link Maneuver#getManeuverCurveWithStableSpeedAndCourseBoundaries(). If there are no
     * previous maneuvers, or the previous maneuver end is not before the start of this maneuver with a distance of at
     * least one second, the result will be {@null}.
     */
    Duration getDurationFromPreviousManeuverEndToManeuverStart();

    /**
     * Gets the average speed and course measured from the end of this maneuver until the start of the next maneuver.
     * Both mentioned maneuver start and end refer to
     * {@link Maneuver#getManeuverCurveWithStableSpeedAndCourseBoundaries(). If there are no following maneuvers, or the
     * the maneuver end of this maneuver is not before the start of the next maneuver with a distance of at least one
     * second, the result will be {@null}.
     */
    SpeedWithBearing getAverageSpeedWithBearingAfter();

    /**
     * Gets the duration from the end of this maneuver until the start of the next maneuver. Both mentioned maneuver
     * start and end refer to {@link Maneuver#getManeuverCurveWithStableSpeedAndCourseBoundaries(). If there are no
     * following maneuvers, or the the maneuver end of this maneuver is not before the start of the next maneuver with a
     * distance of at least one second, the result will be {@null}.
     */
    Duration getDurationFromManeuverEndToNextManeuverStart();
}

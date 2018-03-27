package com.sap.sailing.domain.maneuverdetection;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.tracking.ManeuverCurveBoundaries;
import com.sap.sse.common.Duration;

/**
 * Contains the information about the maneuver curve with unstable course and speed, which is regarded as relevant for
 * maneuver classification algorithms, such as the wind estimation.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface ManeuverCurveWithUnstableCourseAndSpeedWithEstimationData extends ManeuverCurveBoundaries, HasDetailedManeuverLoss {

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

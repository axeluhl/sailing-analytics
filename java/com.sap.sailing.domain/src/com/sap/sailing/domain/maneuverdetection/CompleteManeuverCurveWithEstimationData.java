package com.sap.sailing.domain.maneuverdetection;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.tracking.CompleteManeuverCurve;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;

/**
 * Contains information of a complete maneuver curve which is regarded as relevant for maneuver classification
 * algorithms, such as the wind estimation.
 * 
 * @author Vladislav Chumak (D069712)
 * @see CompleteManeuverCurve
 *
 */
public interface CompleteManeuverCurveWithEstimationData {
    
    @Dimension(messageKey = "ManeuverTypeForCompleteManeuverCurve")
    default ManeuverType getManeuverTypeForCompleteManeuverCurve() {
        if(getJibingCount() > 0 && getTackingCount() > 0) {
            return ManeuverType.PENALTY_CIRCLE;
        }
        if(getTackingCount() > 0) {
            return ManeuverType.TACK;
        }
        if(getJibingCount() > 0) {
            return ManeuverType.JIBE;
        }
        if(getWind() == null) {
            return ManeuverType.UNKNOWN;
        }
        return isManeuverStartsByRunningAwayFromTheWind() ? ManeuverType.BEAR_AWAY : ManeuverType.HEAD_UP;
    }

    /**
     * Gets the information of the main curve of maneuver including its boundaries, course change, and other data
     * relevant for maneuver classification.
     */
    @Connector(messageKey="MainCurve")
    ManeuverMainCurveWithEstimationData getMainCurve();

    /**
     * Gets the information of the curve with unstable course and speed of maneuver including its boundaries, course
     * change, and other data relevant for maneuver classification.
     */
    @Connector(messageKey="CurveWithUnstableCourseAndSpeed")
    ManeuverCurveWithUnstableCourseAndSpeedWithEstimationData getCurveWithUnstableCourseAndSpeed();

    /**
     * Gets the wind measured at the {@link ManeuverMainCurveWithEstimationData#getTimePointOfMaxTurningRate()}. Can be
     * {@code null} in cases, when no wind information is available.
     */
    Wind getWind();

    /**
     * Gets the number of cases, when the boats bow was headed through the wind coming from the front. This information
     * can be used to derive the maneuver type.
     */
    int getTackingCount();

    /**
     * Gets the number of cases, when the boats bow was headed through the wind coming from behind. This information can
     * be used to derive the maneuver type.
     */
    int getJibingCount();

    /**
     * Tells whether the maneuver is started by bearing away or jibing, or not. This information can be used as
     * criterion for distinguishing between head-up and bear-away maneuver types.
     * 
     * @return {@code true} if the maneuver is started by maneuvering towards jibing area, {@code false} if the maneuver
     *         starts by getting the boat closer to the wind.
     */
    @Dimension(messageKey = "ManeuverStartsByRunningAwayFromTheWind")
    boolean isManeuverStartsByRunningAwayFromTheWind();

    /**
     * Gets the relative bearing of the next mark to pass from the boat before the maneuver. The relative bearing is
     * calculated by absolute bearing of next mark from the boat's position minus the boat's course. As the maneuver
     * boundaries, the boundaries of the curve with unstable course and speed are used.
     */
    @Statistic(messageKey="RelativeBearingToNextMarkBeforeManeuver", resultDecimals=1)
    Bearing getRelativeBearingToNextMarkBeforeManeuver();

    /**
     * Gets the relative bearing of the next mark to pass from the boat after the maneuver. The relative bearing is
     * calculated by absolute bearing of next mark from the boat's position minus the boat's course. As the maneuver
     * boundaries, the boundaries of the curve with unstable course and speed are used.
     */
    @Statistic(messageKey="RelativeBearingToNextMarkAfterManeuver", resultDecimals=1)
    Bearing getRelativeBearingToNextMarkAfterManeuver();
    
    /**
     * Gets whether a mark was crossed within the maneuver curve.
     */
    @Dimension(messageKey = "MarkPassing")
    boolean isMarkPassing();
}

package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sse.common.TimePoint;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;

/**
 * Represents a maneuver detected within a competitor track. There are two important sections within a maneuver.
 * <ol>
 * <li>The first section starts from sailing a stable speed at TWA and ends at getting back to a stable speed and target
 * TWA. This section is defined as maneuver curve and its time range is represented by {@code timePointBefore} and
 * {@code timePointAfter}. The target speeds and bearings are represented by {@code speedWithBearingBefore} and
 * {@code speedWithBearingAfter}.</li>
 * <li>The second section is called the main curve and is defined as the section within the maneuver curve, where
 * highest course change has been performed. This means that the main curve is a subset of the maneuver curve which is
 * represented by {@code timePointBeforeMainCurve} and {@code timePointAfterMainCurve}.</li>
 * </ol>
 * The maneuver curve is a expansion of the main curve. The expansion relates speed maxima location before and after
 * main curve. In contrast to maneuver curve, the main curve computation does not take speed into account and is based
 * only on gradual analysis of bearings within the maneuver progress. The main curve is supposed to deliver information
 * about the acceleration during continues turning in the direction of maneuver which can be used for boat class
 * oriented investigations. On the other side, the maneuver curve describes the period where enters from a period of
 * stable TWA and speed in a section of changes and adjustments in order to perform the maneuver. Based on the maneuver
 * curve, the maneuver loss is computed which is regarded as an important measurement feature in order to compare
 * performances of competing racers. In contrast to main curve, the maneuver curve reveals strategic decision making of
 * individual sailors to master a maneuver with minimal maneuver loss.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface Maneuver extends GPSFix {
    /**
     * Gets the type of this maneuver, e.g. whether its a tack, jibe and etc. The maneuver type is determined
     * considering the boat's course change, wind bearing and marks.
     * 
     * @return The type of maneuver
     */
    ManeuverType getType();

    /**
     * Gets the new tack applied after the maneuver. A tack in sailing is defined as the side of the boat (starboard, or
     * port) from which the wind is blowing.
     * 
     * @return The new tack after the performed maneuver
     */
    @Dimension(messageKey = "Tack", ordinal = 13)
    Tack getNewTack();

    /**
     * Gets the the maneuver loss of this maneuver which is the distance projected onto the average course between
     * entering and exiting the maneuver that the boat lost compared to not having maneuvered. The maneuver loss is
     * calculated considering the maneuver curve, which was performed between {@link #getTimePointBefore()} and
     * {@link #getTimePointAfter()}.
     */
    Distance getManeuverLoss();

    /**
     * Gets the time point of the corresponding maneuver. The time point refers to a position within the main curve of
     * maneuver with the highest course change recorded toward the direction of maneuver.
     * 
     * @return The maneuver time point with the highest course change
     */
    TimePoint getTimePoint();

    /**
     * Gets the time point of maneuver start. The time point refers to a position before the main curve where the boat
     * has started to lose speed due to preparations for turning.
     * 
     * @return The time point of maneuver start
     * @see Maneuver
     */
    TimePoint getTimePointBefore();

    /**
     * Gets the time point of maneuver end. The time point refers to a position after the main curve where the boat has
     * accelerated to its target speed after turning.
     * 
     * @return The time point of maneuver end
     * @see Maneuver
     */
    TimePoint getTimePointAfter();

    /**
     * Gets the speed with bearing at maneuver start, which is at {@link #getTimePointBefore()}.
     * 
     * @return The speed with bearing at maneuver start
     * 
     */
    SpeedWithBearing getSpeedWithBearingBefore();

    /**
     * Gets the speed with bearing at maneuver end, which is at {@link #getTimePointAfter()}.
     * 
     * @return The speed with bearing at maneuver end
     */
    SpeedWithBearing getSpeedWithBearingAfter();

    /**
     * Gets the total course change performed within maneuver between {@link #getTimePointBefore()} and
     * {@link #getTimePointAfter()} in degrees. The port side course changes produce a negative value. The value may
     * exceed 360 degrees if the performed maneuver is a penalty circle.
     * 
     * @return The total course change within the whole maneuver in degrees
     */
    @Statistic(messageKey = "DirectionChange", resultDecimals = 2, ordinal = 2)
    double getDirectionChangeInDegrees();

    /**
     * Gets the time point of main curve start, where the boat starts to perform the main turn of the maneuver towards
     * the direction of maneuver.
     * 
     * @return The time point of the main curve start
     * @see Maneuver
     */
    TimePoint getTimePointBeforeMainCurve();

    /**
     * Gets the time point of main curve end, , where the boat finishes to perform the main turn of the maneuver towards
     * the direction of maneuver.
     * 
     * @return The time point of the main curve end
     * @see Maneuver
     */
    TimePoint getTimePointAfterMainCurve();

    /**
     * Gets the total course change performed within the main curve of maneuver between
     * {@link #getTimePointBeforeMainCurve()} and {@link #getTimePointAfterMainCurve()} in degrees. The port side course
     * changes produce a negative value. The value may exceed 360 degrees if the performed maneuver is a penalty circle.
     * 
     * @return The total course change with the main curve in degrees
     */
    @Statistic(messageKey = "DirectionChangeWithinMainCurve", resultDecimals = 2, ordinal = 3)
    double getDirectionChangeWithinMainCurveInDegrees();

}

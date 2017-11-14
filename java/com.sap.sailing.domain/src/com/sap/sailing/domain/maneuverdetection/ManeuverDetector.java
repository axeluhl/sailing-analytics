package com.sap.sailing.domain.maneuverdetection;

import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sse.common.TimePoint;

/**
 * Determines maneuvers performed within a tracked race.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface ManeuverDetector {

    /**
     * @see ManeuverDetector#detectManeuvers(Competitor, Iterable, boolean, TimePoint, TimePoint)
     */
    List<Maneuver> detectManeuvers(Competitor competitor, TimePoint from, TimePoint to, boolean ignoreMarkPassings)
            throws NoWindException;

    /**
     * Tries to detect maneuvers on the <code>competitor</code>'s track based on a number of approximating fixes. The
     * fixes contain bearing information, but this is not the bearing leading to the next approximation fix but the
     * bearing the boat had at the time of the approximating fix which is taken from the original track.
     * <p>
     * 
     * The time period assumed for a maneuver duration is taken from the
     * {@link BoatClass#getApproximateManeuverDurationInMilliseconds() boat class}. If no maneuver is detected, an empty
     * list is returned. Maneuvers can only be expected to be detected if at least three fixes are provided in
     * <code>approximatedFixesToAnalyze</code>. For the inner approximating fixes (all except the first and the last
     * approximating fix), their course changes according to the approximated path (and not the underlying actual
     * tracked fixes) are computed. Subsequent course changes to the same direction are then grouped. Those in closer
     * timely distance than {@link #getApproximateManeuverDurationInMilliseconds()} (including single course changes
     * that have no surrounding other course changes to group) are grouped into one {@link Maneuver}.
     * 
     * @param ignoreMarkPassings
     *            When <code>true</code>, no {@link ManeuverType#MARK_PASSING} maneuvers will be identified, and the
     *            fact that a mark passing would split up what else may be a penalty circle is ignored. This is helpful
     *            for recursive calls, e.g., after identifying a tack and a jibe around a mark passing and trying to
     *            identify for the time before and after the mark passing which maneuvers exist on which side of the
     *            passing.
     * @param earliestManeuverStart
     *            maneuver start will not be before this time point; if a maneuver is found whose time point is at or
     *            after this time point, no matter how close it is, its start regarding speed and course into the
     *            maneuver and the leg before the maneuver is not taken from an earlier time point, even if half the
     *            maneuver duration before the maneuver time point were before this time point.
     * @param latestManeuverEnd
     *            maneuver end will not be after this time point; if a maneuver is found whose time point is at or
     *            before this time point, no matter how close it is, its end regarding speed and course out of the
     *            maneuver and the leg after the maneuver is not taken from a later time point, even if half the
     *            maneuver duration after the maneuver time point were after this time point.
     * 
     * @return an empty list if no maneuver is detected for <code>competitor</code> between <code>from</code> and
     *         <code>to</code>, or else the list of maneuvers detected.
     */
    List<Maneuver> detectManeuvers(Competitor competitor, Iterable<GPSFixMoving> approximatingFixesToAnalyze,
            boolean ignoreMarkPassings, TimePoint earliestManeuverStart, TimePoint latestManeuverEnd)
            throws NoWindException;

}

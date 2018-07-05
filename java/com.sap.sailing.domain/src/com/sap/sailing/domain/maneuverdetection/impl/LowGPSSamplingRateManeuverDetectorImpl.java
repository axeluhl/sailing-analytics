package com.sap.sailing.domain.maneuverdetection.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.CourseChange;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.maneuverdetection.ApproximatedFixesCalculator;
import com.sap.sailing.domain.maneuverdetection.ManeuverDetector;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.ManeuverCurveBoundaries;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.ManeuverCurveBoundariesImpl;
import com.sap.sailing.domain.tracking.impl.ManeuverWithCoarseGrainedBoundariesImpl;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

/**
 * Maneuver detector implementation for GPS tracks with extremely low sampling rate such as 1 fix per 30 seconds.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class LowGPSSamplingRateManeuverDetectorImpl extends AbstractManeuverDetectorImpl implements ManeuverDetector {

    public LowGPSSamplingRateManeuverDetectorImpl(TrackedRace trackedRace, Competitor competitor) {
        super(trackedRace, competitor);
    }

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
    @Override
    public List<Maneuver> detectManeuvers() {
        List<Maneuver> result = new ArrayList<>();
        TrackTimeInfo startAndEndTimePoints = getTrackTimeInfo();
        if (startAndEndTimePoints != null) {
            ApproximatedFixesCalculator approximatedFixesCalculator = new ApproximatedFixesCalculatorImpl(trackedRace,
                    competitor);
            Iterable<GPSFixMoving> approximatedFixes = approximatedFixesCalculator.approximate(
                    startAndEndTimePoints.getTrackStartTimePoint(), startAndEndTimePoints.getTrackEndTimePoint());
            if (Util.size(approximatedFixes) > 2) {
                Iterator<GPSFixMoving> approximationPointsIter = approximatedFixes.iterator();
                GPSFixMoving previous = approximationPointsIter.next();
                GPSFixMoving current = approximationPointsIter.next();
                // the bearings in these variables are between approximation points
                do {
                    GPSFixMoving next = approximationPointsIter.next();
                    SpeedWithBearing speedWithBearingOnApproximationFromPreviousToCurrent = previous
                            .getSpeedAndBearingRequiredToReach(current);
                    SpeedWithBearing speedWithBearingOnApproximationFromCurrentToNext = current
                            .getSpeedAndBearingRequiredToReach(next);
                    CourseChange courseChange = speedWithBearingOnApproximationFromPreviousToCurrent
                            .getCourseChangeRequiredToReach(speedWithBearingOnApproximationFromCurrentToNext);
                    speedWithBearingOnApproximationFromPreviousToCurrent = speedWithBearingOnApproximationFromCurrentToNext;
                    Maneuver maneuver = createManeuverFromGroupOfCourseChanges(competitor,
                            speedWithBearingOnApproximationFromPreviousToCurrent, current,
                            speedWithBearingOnApproximationFromCurrentToNext, courseChange.getCourseChangeInDegrees());
                    result.add(maneuver);
                    previous = current;
                    current = next;
                } while (approximationPointsIter.hasNext());
            }
        }
        return result;
    }

    private Maneuver createManeuverFromGroupOfCourseChanges(Competitor competitor,
            SpeedWithBearing speedWithBearingOnApproximationAtBeginning, GPSFixMoving currentFix,
            SpeedWithBearing speedWithBearingOnApproximationAtEnd, double totalCourseChangeInDegrees) {
        TimePoint maneuverTimePoint = currentFix.getTimePoint();
        Position maneuverPosition = currentFix.getPosition();
        final Wind wind = trackedRace.getWind(maneuverPosition, maneuverTimePoint);
        Tack tackAfterManeuver = null;
        try {
            tackAfterManeuver = wind == null ? null
                    : trackedRace.getTack(maneuverPosition, maneuverTimePoint,
                            speedWithBearingOnApproximationAtEnd.getBearing());
        } catch (NoWindException e) {
        }
        ManeuverType maneuverType;
        ManeuverCurveBoundaries maneuverCurve = new ManeuverCurveBoundariesImpl(
                maneuverTimePoint.minus(getApproximateManeuverDuration().divide(2)),
                maneuverTimePoint.plus(getApproximateManeuverDuration().times(3.0)),
                speedWithBearingOnApproximationAtBeginning, speedWithBearingOnApproximationAtEnd,
                totalCourseChangeInDegrees,
                speedWithBearingOnApproximationAtBeginning.compareTo(speedWithBearingOnApproximationAtBeginning) < 0
                        ? speedWithBearingOnApproximationAtBeginning : speedWithBearingOnApproximationAtEnd);

        if (wind != null) {
            if (getNumberOfTacks(maneuverCurve, wind) > 0) {
                maneuverType = ManeuverType.TACK;
            } else if (getNumberOfJibes(maneuverCurve, wind) > 0) {
                maneuverType = ManeuverType.JIBE;
            } else {
                // heading up or bearing away
                Bearing windBearing = wind.getBearing();
                Bearing toWindBeforeManeuver = windBearing
                        .getDifferenceTo(speedWithBearingOnApproximationAtBeginning.getBearing());
                Bearing toWindAfterManeuver = windBearing
                        .getDifferenceTo(speedWithBearingOnApproximationAtEnd.getBearing());
                maneuverType = Math.abs(toWindBeforeManeuver.getDegrees()) < Math.abs(toWindAfterManeuver.getDegrees())
                        ? ManeuverType.HEAD_UP : ManeuverType.BEAR_AWAY;
            }
        } else {
            // no wind information; marking as UNKNOWN
            maneuverType = ManeuverType.UNKNOWN;
        }
        Maneuver maneuver = new ManeuverWithCoarseGrainedBoundariesImpl(maneuverType, tackAfterManeuver,
                maneuverPosition, maneuverTimePoint, maneuverCurve);
        return maneuver;
    }

}

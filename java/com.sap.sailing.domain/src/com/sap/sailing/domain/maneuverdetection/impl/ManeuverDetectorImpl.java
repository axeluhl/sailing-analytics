package com.sap.sailing.domain.maneuverdetection.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.BearingChangeAnalyzer;
import com.sap.sailing.domain.common.CourseChange;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.impl.CourseChangeImpl;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.maneuverdetection.ManeuverDetector;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.SpeedWithBearingStep;
import com.sap.sailing.domain.tracking.SpeedWithBearingStepsIterable;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.ManeuverImpl;
import com.sap.sailing.domain.tracking.impl.MarkPassingManeuverImpl;
import com.sap.sailing.domain.tracking.impl.SpeedWithBearingStepImpl;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.impl.MillisecondsTimePoint;

/**
 * @author Vladislav Chumak (D069712)
 * @see ManeuverDetector
 *
 */
public class ManeuverDetectorImpl implements ManeuverDetector {

    private static final Logger logger = Logger.getLogger(ManeuverDetectorImpl.class.getName());

    /**
     * Defines the maximal absolute course change velocity in degrees per second that shall be regarded as a stable
     * course.
     */
    private static final double MAX_ABS_COURSE_CHANGE_IN_DEGREES_PER_SECOND_FOR_STABLE_BEARING_ANALYSIS = 2;

    /**
     * Defines the absolute course change in degrees between bearing steps to ignore in order to shorten the
     * approximated span between start and end time of maneuver main curve.
     */
    private static final double MIN_ANGULAR_VELOCITY_FOR_MAIN_CURVE_BOUNDARIES_IN_DEGREES_PER_SECOND = 0.2;

    /**
     * Defines the course change limit toward opposite direction related to the direction of maneuver main curve. If
     * speed maxima or stable bearing analysis produce a curve extension which exceeds this limit, the extension gets
     * rejected.
     */
    private static final double MAX_COURSE_CHANGE_TOWARD_MANEUVER_OPPOSITE_DIRECTION_FOR_CURVE_EXTENSION_IN_DEGREES = 15.0;

    /**
     * Tracked race whose tracks are being processed for maneuver detection.
     */
    private final TrackedRace trackedRace;

    /**
     * Constructs maneuver detector which is supposed to be used for maneuver detection within the provided tracked
     * race.
     * 
     * @param trackedRace
     *            The tracked race whose maneuvers are supposed to be detected
     */
    public ManeuverDetectorImpl(TrackedRace trackedRace) {
        this.trackedRace = trackedRace;
    }

    @Override
    public List<Maneuver> detectManeuvers(Competitor competitor, TimePoint from, TimePoint to,
            boolean ignoreMarkPassings) throws NoWindException {
        return detectManeuvers(competitor,
                trackedRace.approximate(competitor,
                        competitor.getBoat().getBoatClass().getMaximumDistanceForCourseApproximation(), from, to),
                ignoreMarkPassings, from, to);
    }

    @Override
    public List<Maneuver> detectManeuvers(Competitor competitor, Iterable<GPSFixMoving> approximatingFixesToAnalyze,
            boolean ignoreMarkPassings, TimePoint earliestManeuverStart, TimePoint latestManeuverEnd)
            throws NoWindException {
        List<Maneuver> result = new ArrayList<Maneuver>();
        if (Util.size(approximatingFixesToAnalyze) > 2) {
            List<Pair<GPSFixMoving, CourseChange>> courseChangeSequenceInSameDirection = new ArrayList<Pair<GPSFixMoving, CourseChange>>();
            Iterator<GPSFixMoving> approximationPointsIter = approximatingFixesToAnalyze.iterator();
            GPSFixMoving previous = approximationPointsIter.next();
            GPSFixMoving current = approximationPointsIter.next();
            // the bearings in these variables are between approximation points
            SpeedWithBearing speedWithBearingOnApproximationFromPreviousToCurrent = previous
                    .getSpeedAndBearingRequiredToReach(current);
            SpeedWithBearing speedWithBearingOnApproximationFromCurrentToNext; // will certainly be assigned because
                                                                               // iter's collection's size > 2
            do {
                GPSFixMoving next = approximationPointsIter.next();
                // traveling on great circle segments from one approximation point to the next
                speedWithBearingOnApproximationFromCurrentToNext = current.getSpeedAndBearingRequiredToReach(next);
                // compute course change on "approximation track"
                CourseChange courseChange = speedWithBearingOnApproximationFromPreviousToCurrent
                        .getCourseChangeRequiredToReach(speedWithBearingOnApproximationFromCurrentToNext);

                // check for the case where the course change between the approximation fixes may have been >180deg by
                // comparing the direction of the course change on the approximation points with the direction of the
                // course change during the same time range on the original fixes (see also bug 2009):
                TimePoint fromTimePointForOriginalFixesCourseChangeInvestigation = previous.getTimePoint();
                Duration durationFromPreviousToCurrent = fromTimePointForOriginalFixesCourseChangeInvestigation
                        .until(current.getTimePoint());
                Duration maxDurationForOriginalFixesCourseChangeInvestigation = getApproximateManeuverDuration(
                        competitor).divide(2.0);
                if (durationFromPreviousToCurrent.compareTo(maxDurationForOriginalFixesCourseChangeInvestigation) > 0) {
                    fromTimePointForOriginalFixesCourseChangeInvestigation = current.getTimePoint()
                            .minus(maxDurationForOriginalFixesCourseChangeInvestigation);
                }
                Bearing courseChangeOnOriginalFixes = getCourseChange(competitor,
                        fromTimePointForOriginalFixesCourseChangeInvestigation, next.getTimePoint());
                if (Math.abs(courseChangeOnOriginalFixes.getDegrees()) > 180
                        && Math.signum(courseChange.getCourseChangeInDegrees()) != Math
                                .signum(courseChangeOnOriginalFixes.getDegrees())) {
                    courseChange = new CourseChangeImpl(
                            -Math.signum(courseChange.getCourseChangeInDegrees())
                                    * (360.0 - Math.abs(courseChange.getCourseChangeInDegrees())),
                            courseChange.getSpeedChangeInKnots());
                }
                Pair<GPSFixMoving, CourseChange> courseChangeAtFix = new Pair<GPSFixMoving, CourseChange>(current,
                        courseChange);
                if (!courseChangeSequenceInSameDirection.isEmpty() && Math
                        .signum(courseChangeSequenceInSameDirection.get(0).getB().getCourseChangeInDegrees()) != Math
                                .signum(courseChange.getCourseChangeInDegrees())) {
                    // course change in different direction; cluster the course changes in same direction so far, then
                    // start new list
                    List<Maneuver> maneuvers = groupChangesInSameDirectionIntoManeuvers(competitor,
                            courseChangeSequenceInSameDirection, ignoreMarkPassings, earliestManeuverStart,
                            latestManeuverEnd);
                    result.addAll(maneuvers);
                    courseChangeSequenceInSameDirection.clear();
                }
                courseChangeSequenceInSameDirection.add(courseChangeAtFix);
                previous = current;
                current = next;
                speedWithBearingOnApproximationFromPreviousToCurrent = speedWithBearingOnApproximationFromCurrentToNext;
            } while (approximationPointsIter.hasNext());
            if (!courseChangeSequenceInSameDirection.isEmpty()) {
                result.addAll(groupChangesInSameDirectionIntoManeuvers(competitor, courseChangeSequenceInSameDirection,
                        ignoreMarkPassings, earliestManeuverStart, latestManeuverEnd));
            }
        }
        return result;
    }

    /**
     * On <code>competitor</code>'s track iterates the fixes starting after <code>startExclusive</code> until
     * <code>endExclusive</code> or any later fix has been reached and sums up the direction change as a "bearing." A
     * negative sign means a direction change to port, a positive sign means a direction change to starboard.
     */
    private Bearing getCourseChange(Competitor competitor, TimePoint startInclusive, TimePoint endInclusive) {
        SpeedWithBearingStepsIterable speedWithBearingSteps = getSpeedWithBearingSteps(competitor, startInclusive,
                endInclusive);
        double totalCourseChangeInDegrees = 0;
        for (SpeedWithBearingStep step : speedWithBearingSteps) {
            totalCourseChangeInDegrees += step.getCourseChangeInDegrees();
        }
        return new DegreeBearingImpl(totalCourseChangeInDegrees);
    }

    /**
     * Groups the {@link CourseChange} sequence into groups where the times of the fixes at which the course changes
     * took place are no further apart than {@link #getApproximateManeuverDurationInMilliseconds()} milliseconds and
     * where the distances of those course changes are less than three hull lengths apart. For those, a single
     * {@link Maneuver} object is created and added to the resulting list. The maneuver sums up the direction changes of
     * the individual {@link CourseChange} objects. This can result in direction changes of more than 180 degrees in one
     * direction which may, e.g., represent a penalty circle or a mark rounding maneuver. As the maneuver's time point,
     * the average time point of the course changes that went into the maneuver construction is used.
     * <p>
     * 
     * @param courseChangeSequenceInSameDirection
     *            all expected to have equal {@link CourseChange#to()} values
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
     * @return a non-<code>null</code> list
     */
    private List<Maneuver> groupChangesInSameDirectionIntoManeuvers(Competitor competitor,
            List<Pair<GPSFixMoving, CourseChange>> courseChangeSequenceInSameDirection, boolean ignoreMarkPassings,
            TimePoint earliestManeuverStart, TimePoint latestManeuverEnd) throws NoWindException {
        List<Maneuver> result = new ArrayList<Maneuver>();
        List<Pair<GPSFixMoving, CourseChange>> group = new ArrayList<Pair<GPSFixMoving, CourseChange>>();
        if (!courseChangeSequenceInSameDirection.isEmpty()) {
            Distance threeHullLengths = competitor.getBoat().getBoatClass().getHullLength().scale(3);
            Iterator<Pair<GPSFixMoving, CourseChange>> iter = courseChangeSequenceInSameDirection.iterator();
            double totalCourseChangeInDegrees = 0.0;
            do {
                Pair<GPSFixMoving, CourseChange> currentFixAndCourseChange = iter.next();
                if (!group.isEmpty()
                        // TODO use different maneuver times for upwind / reaching / downwind / cross-leg (mark passing)
                        // group contains complete maneuver if the next fix is too late or too far away to belong to the
                        // same maneuver
                        && currentFixAndCourseChange.getA().getTimePoint().asMillis() - group.get(group.size() - 1)
                                .getA().getTimePoint().asMillis() > getApproximateManeuverDuration(competitor)
                                        .asMillis()
                        && currentFixAndCourseChange.getA().getPosition()
                                .getDistance(group.get(group.size() - 1).getA().getPosition())
                                .compareTo(threeHullLengths) > 0) {
                    // if next is more than approximate maneuver duration later and further apart than three hull
                    // lengths, turn the current group into a maneuver and add to result
                    Util.addAll(createManeuverFromGroupOfCourseChanges(competitor, group,
                            totalCourseChangeInDegrees < 0 ? NauticalSide.PORT : NauticalSide.STARBOARD,
                            earliestManeuverStart, latestManeuverEnd), result);
                    group.clear();
                    totalCourseChangeInDegrees = 0.0;
                }
                totalCourseChangeInDegrees += currentFixAndCourseChange.getB().getCourseChangeInDegrees();
                group.add(currentFixAndCourseChange);
                // change
            } while (iter.hasNext());
            if (!group.isEmpty()) {
                Util.addAll(createManeuverFromGroupOfCourseChanges(competitor, group,
                        totalCourseChangeInDegrees < 0 ? NauticalSide.PORT : NauticalSide.STARBOARD,
                        earliestManeuverStart, latestManeuverEnd), result);
            }
        }
        return result;
    }

    /**
     * Creates maneuvers from the provided {@code group} of douglas peucker fixes considering the provided time range
     * limit. This method might return zero or more maneuvers. The maneuvers are determined by the following work-flow:
     * <ol>
     * <li>Main curve of maneuver within the time range of douglas peucker fixes +-
     * ({@link BoatClass#getApproximateManeuverDuration() maneuver duration}{@code  / 2}) is determined. The main curve
     * is defined as the section of maneuver with the highest absolute course change towards the provided
     * {@code maneuverDirection} ({@link Maneuver more info}).</li>
     * <li>Maneuver start and end with stable speed and course are determined by analysis of speed maxima starting
     * before and after the main curve, followed by extension to the points with stable course
     * ({@link #computeManeuverDetails(Competitor, CurveDetailsWithBearingSteps, TimePoint, TimePoint) more info})</li>
     * <li>The maneuver type is determined considering the maneuver's main curve, marks and wind direction</li>
     * </ol>
     * 
     * @param competitor
     *            The competitor whose maneuvers are being determined
     * @param douglasPeuckerFixesGroup
     *            The douglas peucker fixes which may represent the maneuver basis
     * @param maneuverDirection
     *            The course change direction within douglas peucker fixes.
     * @param earliestManeuverStart
     *            Maneuver start will not be before this time point; if a maneuver is found whose time point is at or
     *            after this time point, no matter how close it is, its start regarding speed and course into the
     *            maneuver and the leg before the maneuver is not taken from an earlier time point, even if half the
     *            maneuver duration before the maneuver time point were before this time point.
     * @param latestManeuverEnd
     *            Maneuver end will not be after this time point; if a maneuver is found whose time point is at or
     *            before this time point, no matter how close it is, its end regarding speed and course out of the
     *            maneuver and the leg after the maneuver is not taken from a later time point, even if half the
     *            maneuver duration after the maneuver time point were after this time point.
     * @return The derived list maneuvers. The maneuvers count {@code x} may be {@code x >= 0}.
     * @throws NoWindException
     *             When no wind information during maneuver performance is available
     * @see #groupChangesInSameDirectionIntoManeuvers(Competitor, List, boolean, TimePoint, TimePoint)
     * @see #computeManeuverDetails(Competitor, CurveDetailsWithBearingSteps, TimePoint, TimePoint)
     */
    private Iterable<Maneuver> createManeuverFromGroupOfCourseChanges(Competitor competitor,
            List<Pair<GPSFixMoving, CourseChange>> douglasPeuckerFixesGroup, NauticalSide maneuverDirection,
            TimePoint earliestManeuverStart, TimePoint latestManeuverEnd) throws NoWindException {
        List<Maneuver> result = new ArrayList<>();
        TimePoint earliestTimePointBeforeManeuver = Collections.max(
                Arrays.asList(new MillisecondsTimePoint(douglasPeuckerFixesGroup.get(0).getA().getTimePoint().asMillis()
                        - getApproximateManeuverDuration(competitor).asMillis() / 2), earliestManeuverStart));
        TimePoint latestTimePointAfterManeuver = Collections.min(Arrays.asList(
                new MillisecondsTimePoint(douglasPeuckerFixesGroup.get(douglasPeuckerFixesGroup.size() - 1).getA()
                        .getTimePoint().asMillis() + getApproximateManeuverDuration(competitor).asMillis() / 2),
                latestManeuverEnd));

        CurveDetailsWithBearingSteps maneuverMainCurveDetails = computeManeuverMainCurveDetails(competitor,
                earliestTimePointBeforeManeuver, latestTimePointAfterManeuver, maneuverDirection);
        if (maneuverMainCurveDetails == null) {
            return result;
        }
        CurveDetails maneuverDetails = computeManeuverDetails(competitor, maneuverMainCurveDetails,
                earliestManeuverStart, latestManeuverEnd);
        final GPSFixTrack<Competitor, GPSFixMoving> competitorTrack = trackedRace.getTrack(competitor);
        Position maneuverPosition = competitorTrack.getEstimatedPosition(maneuverDetails.getTimePoint(),
                /* extrapolate */false);
        final Wind wind = trackedRace.getWind(maneuverPosition, maneuverDetails.getTimePoint());
        final Tack tackAfterManeuver = wind == null ? null
                : trackedRace.getTack(maneuverPosition, maneuverDetails.getTimePointAfter(),
                        maneuverDetails.getSpeedWithBearingAfter().getBearing());
        ManeuverType maneuverType;
        Distance maneuverLoss = null;
        // the TrackedLegOfCompetitor variables may be null, e.g., in case the time points are before or after the race
        TrackedLegOfCompetitor legBeforeManeuver = trackedRace.getTrackedLeg(competitor,
                maneuverMainCurveDetails.getTimePointBefore());
        TrackedLegOfCompetitor legAfterManeuver = trackedRace.getTrackedLeg(competitor,
                maneuverMainCurveDetails.getTimePointAfter());
        Waypoint waypointPassed = null; // set for MARK_PASSING maneuvers only
        NauticalSide sideToWhichWaypointWasPassed = null; // set for MARK_PASSING maneuvers only
        // check for mask passing first; a tacking / jibe-setting mark rounding thus takes precedence over being
        // detected as a penalty circle
        final TimePoint markPassingTimePoint;
        if (legBeforeManeuver != legAfterManeuver
                // a maneuver at the start line is not to be considered a MARK_PASSING maneuver; show a tack as a tack
                && legAfterManeuver != null
                && legAfterManeuver.getLeg().getFrom() != trackedRace.getRace().getCourse().getFirstWaypoint()) {
            waypointPassed = legAfterManeuver.getLeg().getFrom();
            MarkPassing markPassing = trackedRace.getMarkPassing(competitor, waypointPassed);
            markPassingTimePoint = markPassing != null ? markPassing.getTimePoint() : maneuverDetails.getTimePoint();
            Position markPassingPosition = markPassing != null
                    ? competitorTrack.getEstimatedPosition(markPassingTimePoint, /* extrapolate */false)
                    : maneuverPosition;
            sideToWhichWaypointWasPassed = maneuverDirection;
            // produce an additional mark passing maneuver; continue to analyze to catch jibe sets and kiwi drops
            result.add(new MarkPassingManeuverImpl(ManeuverType.MARK_PASSING, tackAfterManeuver, markPassingPosition,
                    maneuverLoss, markPassingTimePoint, maneuverDetails.getTimePointBefore(),
                    maneuverDetails.getTimePointAfter(), maneuverDetails.getSpeedWithBearingBefore(),
                    maneuverDetails.getSpeedWithBearingAfter(), maneuverDetails.getTotalCourseChangeInDegrees(),
                    maneuverMainCurveDetails.getTimePointBefore(), maneuverMainCurveDetails.getTimePointAfter(),
                    maneuverMainCurveDetails.getTotalCourseChangeInDegrees(),
                    maneuverMainCurveDetails.getMaxAngularVelocityInDegreesPerSecond(), waypointPassed,
                    sideToWhichWaypointWasPassed));
        } else {
            markPassingTimePoint = null;
        }
        BearingChangeAnalyzer bearingChangeAnalyzer = BearingChangeAnalyzer.INSTANCE;
        final Bearing courseBeforeManeuver = maneuverMainCurveDetails.getSpeedWithBearingBefore().getBearing();
        final Bearing courseAfterManeuver = maneuverMainCurveDetails.getSpeedWithBearingAfter().getBearing();
        final double mainCurveTotalCourseChangeInDegrees = maneuverMainCurveDetails.getTotalCourseChangeInDegrees();
        int numberOfJibes = wind == null ? 0
                : bearingChangeAnalyzer.didPass(courseBeforeManeuver, mainCurveTotalCourseChangeInDegrees,
                        courseAfterManeuver, wind.getBearing());
        int numberOfTacks = wind == null ? 0
                : bearingChangeAnalyzer.didPass(courseBeforeManeuver, mainCurveTotalCourseChangeInDegrees,
                        courseAfterManeuver, wind.getFrom());
        if (markPassingTimePoint != null && (numberOfTacks + numberOfJibes > 0)) {
            // In case of a mark passing we need to split the maneuver analysis into the phase before and after
            // the mark passing. First of all, this is important to identify the correct maneuver time point for
            // each tack and jibe, second it is essential to call a penalty which is only the case if the tack and
            // the jibe are on the same side of the mark passing; otherwise this may have been a jibe set or a
            // kiwi drop.
            // Therefore, we recursively detect the maneuvers for the segment before and the segment after the
            // mark passing and add the results to our result.
            result.addAll(detectManeuvers(competitor, maneuverDetails.getTimePointBefore(),
                    markPassingTimePoint.minus(1), /* ignoreMarkPassings */ true));
            result.addAll(detectManeuvers(competitor, markPassingTimePoint.plus(1), maneuverDetails.getTimePointAfter(),
                    /* ignoreMarkPassings */ true));
        } else {
            // Either there was no mark passing, or the mark passing was not accompanied by a tack or a jibe.
            // For the first tack/jibe combination (they must alternate because the course changes in the same direction
            // and
            // the wind is considered sufficiently stable to not allow for two successive tacks or two successive jibes)
            // we create a PENALTY_CIRCLE maneuver and recurse for the time interval after the first penalty circle has
            // completed.
            if (numberOfTacks > 0 && numberOfJibes > 0 && markPassingTimePoint == null) {
                TimePoint firstPenaltyCircleCompletedAt = getTimePointOfCompletionOfFirstPenaltyCircle(competitor,
                        maneuverMainCurveDetails.getTimePointBefore(), courseBeforeManeuver,
                        maneuverMainCurveDetails.getSpeedWithBearingSteps(), wind);
                if (firstPenaltyCircleCompletedAt == null) {
                    // This should really not happen!
                    logger.warning(
                            "Maneuver detection has failed to process penalty circle maneuver correctly, because getTimePointOfCompletionOfFirstPenaltyCircle() returned null. Race-Id: "
                                    + trackedRace.getRace().getId() + ", Competitor: " + competitor.getName()
                                    + ", Time point before maneuver: " + maneuverDetails.getTimePointBefore());
                    // Use already detected maneuver details as fallback data to prevent Nullpointer
                    firstPenaltyCircleCompletedAt = maneuverDetails.getTimePointAfter();
                }
                maneuverType = ManeuverType.PENALTY_CIRCLE;
                CurveDetailsWithBearingSteps refinedPenaltyMainCurveDetails = computeManeuverMainCurveDetails(
                        competitor, maneuverMainCurveDetails.getTimePointBefore(), firstPenaltyCircleCompletedAt,
                        maneuverDirection);

                CurveDetails refinedPenaltyDetails;
                if (refinedPenaltyMainCurveDetails == null) {
                    // This should really not happen!
                    logger.warning(
                            "Maneuver detection has failed to process penalty circle maneuver correctly, because refinedPenaltyMainCurveDetails computation returned null. Race-Id: "
                                    + trackedRace.getRace().getId() + ", Competitor: " + competitor.getName()
                                    + ", Time point before maneuver: " + maneuverDetails.getTimePointBefore());
                    // Use already detected maneuver main curve as fallback data to prevent Nullpointer
                    refinedPenaltyMainCurveDetails = maneuverMainCurveDetails;
                    refinedPenaltyDetails = maneuverDetails;
                    firstPenaltyCircleCompletedAt = maneuverDetails.getTimePointAfter();
                } else {
                    refinedPenaltyDetails = computeManeuverDetails(competitor, refinedPenaltyMainCurveDetails,
                            maneuverDetails.getTimePointBefore(), firstPenaltyCircleCompletedAt);
                }
                if (legBeforeManeuver != null) {
                    maneuverLoss = legBeforeManeuver.getManeuverLoss(maneuverDetails.getTimePointBefore(),
                            maneuverDetails.getTimePoint(), firstPenaltyCircleCompletedAt);
                }
                Position penaltyPosition = competitorTrack.getEstimatedPosition(refinedPenaltyDetails.getTimePoint(),
                        /* extrapolate */ false);
                final Maneuver maneuver = new ManeuverImpl(maneuverType, tackAfterManeuver, penaltyPosition,
                        maneuverLoss, refinedPenaltyDetails.getTimePoint(), refinedPenaltyDetails.getTimePointBefore(),
                        refinedPenaltyDetails.getTimePointAfter(), refinedPenaltyDetails.getSpeedWithBearingBefore(),
                        refinedPenaltyDetails.getSpeedWithBearingAfter(),
                        refinedPenaltyDetails.getTotalCourseChangeInDegrees(),
                        refinedPenaltyMainCurveDetails.getTimePointBefore(),
                        refinedPenaltyMainCurveDetails.getTimePointAfter(),
                        refinedPenaltyMainCurveDetails.getTotalCourseChangeInDegrees(),
                        refinedPenaltyMainCurveDetails.getMaxAngularVelocityInDegreesPerSecond());
                result.add(maneuver);
                // after we've "consumed" one tack and one jibe, recursively find more maneuvers if tacks and/or jibes
                // remain
                if (numberOfTacks > 1 || numberOfJibes > 1) {
                    result.addAll(detectManeuvers(competitor, firstPenaltyCircleCompletedAt,
                            maneuverDetails.getTimePointAfter(), /* ignoreMarkPassings */ true));
                }
            } else {
                if (numberOfTacks > 0) {
                    maneuverType = ManeuverType.TACK;
                    if (legBeforeManeuver != null) {
                        maneuverLoss = legBeforeManeuver.getManeuverLoss(maneuverDetails.getTimePointBefore(),
                                maneuverDetails.getTimePoint(), maneuverDetails.getTimePointAfter());
                    }
                } else if (numberOfJibes > 0) {
                    maneuverType = ManeuverType.JIBE;
                    if (legBeforeManeuver != null) {
                        maneuverLoss = legBeforeManeuver.getManeuverLoss(maneuverDetails.getTimePointBefore(),
                                maneuverDetails.getTimePoint(), maneuverDetails.getTimePointAfter());
                    }
                } else {
                    if (wind != null) {
                        // heading up or bearing away
                        Bearing windBearing = wind.getBearing();
                        Bearing toWindBeforeManeuver = windBearing
                                .getDifferenceTo(maneuverMainCurveDetails.getSpeedWithBearingBefore().getBearing());
                        Bearing toWindAfterManeuver = windBearing
                                .getDifferenceTo(maneuverMainCurveDetails.getSpeedWithBearingAfter().getBearing());
                        maneuverType = Math.abs(toWindBeforeManeuver.getDegrees()) < Math
                                .abs(toWindAfterManeuver.getDegrees()) ? ManeuverType.HEAD_UP : ManeuverType.BEAR_AWAY;
                        // treat maneuver main curve details as main maneuver details, because the detected maneuver is
                        // either HEAD_UP or BEAR_AWAY
                        maneuverDetails = maneuverMainCurveDetails;
                    } else {
                        // no wind information; marking as UNKNOWN
                        maneuverType = ManeuverType.UNKNOWN;
                        if (legBeforeManeuver != null) {
                            maneuverLoss = legBeforeManeuver.getManeuverLoss(maneuverDetails.getTimePointBefore(),
                                    maneuverDetails.getTimePoint(), maneuverDetails.getTimePointAfter());
                        }
                    }
                }
                final Maneuver maneuver = new ManeuverImpl(maneuverType, tackAfterManeuver, maneuverPosition,
                        maneuverLoss, maneuverDetails.getTimePoint(), maneuverDetails.getTimePointBefore(),
                        maneuverDetails.getTimePointAfter(), maneuverDetails.getSpeedWithBearingBefore(),
                        maneuverDetails.getSpeedWithBearingAfter(), maneuverDetails.getTotalCourseChangeInDegrees(),
                        maneuverMainCurveDetails.getTimePointBefore(), maneuverMainCurveDetails.getTimePointAfter(),
                        maneuverMainCurveDetails.getTotalCourseChangeInDegrees(),
                        maneuverMainCurveDetails.getMaxAngularVelocityInDegreesPerSecond());
                result.add(maneuver);
            }
        }
        return result;
    }

    /**
     * Starting at <code>timePointBeforeManeuver</code>, and assuming that the group of
     * <code>approximatedFixesAndCourseChanges</code> contains at least a tack and a jibe, finds the approximated fix's
     * time point at which one tack and one jibe have been completed and for which the total course change is as close
     * as possible to 360 degrees.
     */
    private TimePoint getTimePointOfCompletionOfFirstPenaltyCircle(Competitor competitor,
            TimePoint timePointBeforeManeuver, Bearing courseBeforeManeuver,
            SpeedWithBearingStepsIterable maneuverBearingSteps, Wind wind) {
        double totalCourseChangeInDegrees = 0;
        double bestTotalCourseChangeInDegrees = 0; // this should be as close as possible to 360� after one tack and one
                                                   // gybe
        BearingChangeAnalyzer bearingChangeAnalyzer = BearingChangeAnalyzer.INSTANCE;
        Bearing newCourse = courseBeforeManeuver;
        TimePoint result = null;
        boolean firstEntry = true;
        for (SpeedWithBearingStep fixAndCourseChange : maneuverBearingSteps) {
            if (firstEntry) {
                firstEntry = false;
                continue;
            }
            totalCourseChangeInDegrees += fixAndCourseChange.getCourseChangeInDegrees();
            newCourse = newCourse.add(new DegreeBearingImpl(fixAndCourseChange.getCourseChangeInDegrees()));
            int numberOfJibes = bearingChangeAnalyzer.didPass(courseBeforeManeuver, totalCourseChangeInDegrees,
                    newCourse, wind.getBearing());
            int numberOfTacks = bearingChangeAnalyzer.didPass(courseBeforeManeuver, totalCourseChangeInDegrees,
                    newCourse, wind.getFrom());
            if (numberOfJibes > 0 && numberOfTacks > 0) {
                if (numberOfJibes > 1 || numberOfTacks > 1) {
                    // It could be that one or both numbers increased to greater than 1 from 0. In this case
                    // we want to find the point between the completion of the penalty and the next maneuver
                    // which increases one of the counters to 2 and use that time point as the result:
                    if (result == null) {
                        // It could be that both numbers increased, and one was 1 before, so now we have 1 and 2. But
                        // we can't split it up finer than two fixes, so we'll use the time point between the last two
                        // fixes
                        // instead:
                        result = fixAndCourseChange.getTimePoint();
                    }
                    break; // don't continue into a subsequent tack/gybe sailed in conjunction with the penalty or
                           // starting the next circle
                }
                if (Math.abs(360 - Math.abs(totalCourseChangeInDegrees)) < (Math
                        .abs(360 - Math.abs(bestTotalCourseChangeInDegrees)))) {
                    bestTotalCourseChangeInDegrees = totalCourseChangeInDegrees;
                    result = fixAndCourseChange.getTimePoint();
                } else {
                    break; // not getting closer but further away from 360�
                }
            }
        }
        return result;
    }

    /**
     * Computes details of the {@link Maneuver main curve of maneuver}, such as maneuver entering and exiting time point
     * with speed and bearing, time point with the highest turning rate (maneuver climax), total course change, and
     * speed with bearing steps of main curve. The maneuver section with the highest sum of absolute course change
     * angles between bearing steps is defined as the main curve section ({@link Maneuver more info})s.
     * 
     * @param competitor
     *            The competitor whose maneuvers are being determined
     * @param timePointBeforeManeuver
     * @param timePointAfterManeuver
     *            The time range which will be gradually decreased in order to locate the section with the highest
     *            course change towards the target course change direction
     * @param maneuverDirection
     *            The target course change direction for the main curve to determine
     * @return The details of the maneuver main curve
     */
    private CurveDetailsWithBearingSteps computeManeuverMainCurveDetails(Competitor competitor,
            TimePoint timePointBeforeManeuver, TimePoint timePointAfterManeuver, NauticalSide maneuverDirection) {
        SpeedWithBearingStepsIterable stepsToAnalyze = getSpeedWithBearingSteps(competitor, timePointBeforeManeuver,
                timePointAfterManeuver);
        CurveDetails maneuverMainCurveDetails = computeManeuverMainCurve(stepsToAnalyze, maneuverDirection);
        if (maneuverMainCurveDetails == null) {
            return null;
        }
        SpeedWithBearingStepsIterable maneuverMainCurveSpeedWithBearingSteps = getSpeedWithBearingStepsWithinTimeRange(
                stepsToAnalyze, maneuverMainCurveDetails.getTimePointBefore(),
                maneuverMainCurveDetails.getTimePointAfter());
        return new CurveDetailsWithBearingSteps(maneuverMainCurveDetails.getTimePointBefore(),
                maneuverMainCurveDetails.getTimePointAfter(), maneuverMainCurveDetails.getTimePoint(),
                maneuverMainCurveDetails.getSpeedWithBearingBefore(),
                maneuverMainCurveDetails.getSpeedWithBearingAfter(),
                maneuverMainCurveDetails.getTotalCourseChangeInDegrees(),
                maneuverMainCurveDetails.getMaxAngularVelocityInDegreesPerSecond(),
                maneuverMainCurveSpeedWithBearingSteps);
    }

    /**
     * Gets a new list with bearing steps which are lying between provided time range (including the boundaries). To get
     * the steps, performance costy call to {@link GPSFixTrack#getSpeedWithBearingSteps(TimePoint, TimePoint, Duration)}
     * is made.
     */
    private SpeedWithBearingStepsIterable getSpeedWithBearingSteps(Competitor competitor,
            TimePoint timePointBeforeManeuver, TimePoint timePointAfterManeuver) {
        GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
        SpeedWithBearingStepsIterable stepsToAnalyze = track.getSpeedWithBearingSteps(timePointBeforeManeuver,
                timePointAfterManeuver);
        return stepsToAnalyze;
    }

    /**
     * Computes the details of maneuver such as maneuver entering and exiting time point with speed and bearing, time
     * point with the highest turning rate (maneuver climax) and total course change. The provided details of maneuver
     * main curve are used as minimal maneuver section which gets expanded by analyzing the speed and bearing trend
     * regarding stability before and after the main curve of maneuver. The goal is to determine the maneuver entering
     * and exiting time points such that the speed and course values ideally represent stable segments leading into and
     * out of the maneuver. It is assumed that before maneuver the speed starts to slow down. Thus, in order to
     * approximate the beginning time point of the maneuver, the speed maximum is determined throughout forward in time
     * iteration of speed steps starting from time point of main curve beginning. From the determined speed maximum, the
     * iteration continues until the point, when the bearing changes occur only with a maximum of
     * {@value #MAX_ABS_COURSE_CHANGE_IN_DEGREES_PER_SECOND_FOR_STABLE_BEARING_ANALYSIS} degrees per second, which is
     * regarded as a stable course. The exiting time point of maneuver is approximated analogously by speed maximum
     * determination throughout backward in time iteration of speed steps starting from time of main curve end, followed
     * by a search for a point with stable course.
     * 
     * @param competitor
     *            The competitor whose maneuvers are being determined
     * @param maneuverMainCurveDetails
     *            The details of the main curve, ideally computed by
     *            {@link #computeManeuverMainCurveDetails(Competitor, TimePoint, TimePoint, NauticalSide)}
     * @param earliestManeuverStart
     *            Maneuver start will not be before this time point
     * @param latestManeuverEnd
     *            Maneuver end will not be after this time point
     * @return The details of the maneuver
     */
    private CurveDetails computeManeuverDetails(Competitor competitor,
            CurveDetailsWithBearingSteps maneuverMainCurveDetails, TimePoint earliestManeuverStart,
            TimePoint latestManeuverEnd) {
        CurveBoundaryExtension beforeManeuverSectionExtension = expandBeforeManeuverSectionBySpeedAndBearingTrendAnalysis(
                competitor, maneuverMainCurveDetails, earliestManeuverStart);
        CurveBoundaryExtension afterManeuverSectionExtension = expandAfterManeuverSectionBySpeedAndBearingTrendAnalysis(
                competitor, maneuverMainCurveDetails, latestManeuverEnd);
        double totalCourseChangeInDegrees = beforeManeuverSectionExtension.getCourseChangeInDegreesWithinExtensionArea()
                + maneuverMainCurveDetails.getTotalCourseChangeInDegrees()
                + afterManeuverSectionExtension.getCourseChangeInDegreesWithinExtensionArea();
        return new CurveDetails(beforeManeuverSectionExtension.getExtensionTimePoint(),
                afterManeuverSectionExtension.getExtensionTimePoint(), maneuverMainCurveDetails.getTimePoint(),
                beforeManeuverSectionExtension.getSpeedWithBearingAtExtensionTimePoint(),
                afterManeuverSectionExtension.getSpeedWithBearingAtExtensionTimePoint(), totalCourseChangeInDegrees,
                maneuverMainCurveDetails.getMaxAngularVelocityInDegreesPerSecond());
    }

    /**
     * Determines the start of maneuver by analysis of speed and bearing trend starting from the start of provided
     * maneuver main curve. Firstly, speed maximum is located by iterating through the speed with bearings steps
     * backward in time, starting from the time point of main curve start {@code t}. In interval {@code [t -}
     * {@link BoatClass#getApproximateManeuverDurationInMilliseconds() approx. maneuver duration} {@code / 8; t]} global
     * speed maximum is considered, whereas in interval {@code [t -}
     * {@link BoatClass#getApproximateManeuverDurationInMilliseconds() approx. maneuver duration} {@code ; t - }
     * {@link BoatClass#getApproximateManeuverDurationInMilliseconds() approx. maneuver duration} {@code / 8)} the
     * search continues only if the speed keeps rising. After the time point with speed maximum {@code t'} is
     * determined, the course changes get analyzed starting from {@code t'} until {@code (t -}
     * {@link BoatClass#getApproximateManeuverDurationInMilliseconds() approx. maneuver duration}{@code )} in order to
     * locate the point where the bearing starts to change with a rate of maximal
     * {@value #MAX_ABS_COURSE_CHANGE_IN_DEGREES_PER_SECOND_FOR_STABLE_BEARING_ANALYSIS} degrees per second, which is
     * regarded as a stable course.
     * 
     * @param competitor
     *            The competitor whose maneuvers are being determined
     * @param maneuverMainCurveDetails
     *            The details of the main curve, ideally computed by
     *            {@link #computeManeuverMainCurveDetails(Competitor, TimePoint, TimePoint, NauticalSide)}
     * @param earliestManeuverStart
     *            Maneuver start will not be before this time point
     * @return The time point and speed at located step with speed maximum, as well as the total course change from the
     *         step iteration started until the step with the speed maximum
     * @see #computeManeuverDetails(Competitor, CurveDetailsWithBearingSteps, TimePoint, TimePoint)
     */
    private CurveBoundaryExtension expandBeforeManeuverSectionBySpeedAndBearingTrendAnalysis(Competitor competitor,
            CurveDetailsWithBearingSteps maneuverMainCurveDetails, TimePoint earliestManeuverStart) {
        Duration approximateManeuverDuration = getApproximateManeuverDuration(competitor);
        Duration minDurationForSpeedTrendAnalysis = approximateManeuverDuration.divide(8.0);
        Duration maxDurationForSpeedTrendAnalysis = approximateManeuverDuration;
        TimePoint latestTimePointForSpeedTrendAnalysis = maneuverMainCurveDetails.getTimePointBefore();
        TimePoint earliestTimePointForSpeedTrendAnalysis = latestTimePointForSpeedTrendAnalysis
                .minus(maxDurationForSpeedTrendAnalysis);
        if (earliestTimePointForSpeedTrendAnalysis.before(earliestManeuverStart)) {
            earliestTimePointForSpeedTrendAnalysis = earliestManeuverStart;
        }
        TimePoint timePointSinceGlobalMaximumSearch = latestTimePointForSpeedTrendAnalysis
                .minus(minDurationForSpeedTrendAnalysis);
        SpeedWithBearingStepsIterable stepsToAnalyze = getSpeedWithBearingSteps(competitor,
                earliestTimePointForSpeedTrendAnalysis, latestTimePointForSpeedTrendAnalysis);
        CurveBoundaryExtension maneuverStart = findSpeedMaximum(stepsToAnalyze, true,
                timePointSinceGlobalMaximumSearch);
        if (checkCourseChangeLimitExceededForCurveExtension(maneuverMainCurveDetails, maneuverStart)) {
            maneuverStart = null;
        }
        TimePoint stableBearingAnalysisUntil = maneuverStart == null ? maneuverMainCurveDetails.getTimePointBefore()
                : maneuverStart.getExtensionTimePoint();
        double courseChangeSinceManeuverMainCurveInDegrees = maneuverStart == null ? 0
                : maneuverStart.getCourseChangeInDegreesWithinExtensionArea();
        stepsToAnalyze = getSpeedWithBearingStepsWithinTimeRange(stepsToAnalyze, earliestTimePointForSpeedTrendAnalysis,
                stableBearingAnalysisUntil);
        CurveBoundaryExtension stableBearingExtension = findStableBearingWithMaxAbsCourseChangeSpeed(stepsToAnalyze,
                true, MAX_ABS_COURSE_CHANGE_IN_DEGREES_PER_SECOND_FOR_STABLE_BEARING_ANALYSIS);
        if (stableBearingExtension != null
                && !checkCourseChangeLimitExceededForCurveExtension(maneuverMainCurveDetails, stableBearingExtension)) {
            maneuverStart = stableBearingExtension;
            courseChangeSinceManeuverMainCurveInDegrees += stableBearingExtension
                    .getCourseChangeInDegreesWithinExtensionArea();
        }
        return maneuverStart != null
                ? new CurveBoundaryExtension(maneuverStart.getExtensionTimePoint(),
                        maneuverStart.getSpeedWithBearingAtExtensionTimePoint(),
                        courseChangeSinceManeuverMainCurveInDegrees
                                + maneuverStart.getCourseChangeInDegreesWithinExtensionArea())
                : new CurveBoundaryExtension(maneuverMainCurveDetails.getTimePointBefore(),
                        maneuverMainCurveDetails.getSpeedWithBearingBefore(), 0);
    }

    private boolean checkCourseChangeLimitExceededForCurveExtension(
            CurveDetailsWithBearingSteps maneuverMainCurveDetails, CurveBoundaryExtension curveBoundaryExtension) {
        if (curveBoundaryExtension == null) {
            return false;
        }
        return curveBoundaryExtension.getCourseChangeInDegreesWithinExtensionArea()
                * maneuverMainCurveDetails.getTotalCourseChangeInDegrees() < 0
                && Math.abs(curveBoundaryExtension
                        .getCourseChangeInDegreesWithinExtensionArea()) > MAX_COURSE_CHANGE_TOWARD_MANEUVER_OPPOSITE_DIRECTION_FOR_CURVE_EXTENSION_IN_DEGREES;
    }

    /**
     * Determines the end of maneuver by analysis of speed and bearing trend starting from the end of provided maneuver
     * main curve. Firstly, speed maximum is located by iterating through the speed with bearings steps forward in time,
     * starting from the time point of main curve end {@code t}. In interval {@code [t; t +}
     * {@link BoatClass#getApproximateManeuverDurationInMilliseconds() approx. maneuver duration} {@code ]} global speed
     * maximum is considered, whereas in interval {@code (t +}
     * {@link BoatClass#getApproximateManeuverDurationInMilliseconds() approx. maneuver duration} {@code ; t + }
     * {@link BoatClass#getApproximateManeuverDurationInMilliseconds() approx. maneuver duration} {@code * 3)} the
     * search continues only if the speed keeps rising. After the time point with speed maximum {@code t'} is
     * determined, the course changes get analyzed starting from {@code t'} until {@code (t +}
     * {@link BoatClass#getApproximateManeuverDurationInMilliseconds() approx. maneuver duration} {@code * 3)} in order
     * to locate the point where the bearing starts to change with a rate of maximal
     * {@value #MAX_ABS_COURSE_CHANGE_IN_DEGREES_PER_SECOND_FOR_STABLE_BEARING_ANALYSIS} degrees per second, which is
     * regarded as a stable course.
     * 
     * @param competitor
     *            The competitor whose maneuvers are being determined
     * @param maneuverMainCurveDetails
     *            The details of the main curve, ideally computed by
     *            {@link #computeManeuverMainCurveDetails(Competitor, TimePoint, TimePoint, NauticalSide)}
     * @param latestManeuverEnd
     *            Maneuver end will not be after this time point
     * @return The time point and speed at located step with speed maximum, as well as the total course change from the
     *         step iteration started until the step with the speed maximum
     * @see #computeManeuverDetails(Competitor, CurveDetailsWithBearingSteps, TimePoint, TimePoint)
     */
    private CurveBoundaryExtension expandAfterManeuverSectionBySpeedAndBearingTrendAnalysis(Competitor competitor,
            CurveDetailsWithBearingSteps maneuverMainCurveDetails, TimePoint latestManeuverEnd) {
        Duration approximateManeuverDuration = getApproximateManeuverDuration(competitor);
        Duration minDurationForSpeedTrendAnalysis = approximateManeuverDuration;
        Duration maxDurationForSpeedTrendAnalysis = approximateManeuverDuration.times(3.0);
        TimePoint earliestTimePointForSpeedTrendAnalysis = maneuverMainCurveDetails.getTimePointAfter();
        TimePoint latestTimePointForSpeedTrendAnalysis = earliestTimePointForSpeedTrendAnalysis
                .plus(maxDurationForSpeedTrendAnalysis);
        if (latestTimePointForSpeedTrendAnalysis.after(latestManeuverEnd)) {
            latestTimePointForSpeedTrendAnalysis = latestManeuverEnd;
        }
        TimePoint timePointBeforeLocalMaximumSearch = earliestTimePointForSpeedTrendAnalysis
                .plus(minDurationForSpeedTrendAnalysis);
        SpeedWithBearingStepsIterable stepsToAnalyze = getSpeedWithBearingSteps(competitor,
                earliestTimePointForSpeedTrendAnalysis, latestTimePointForSpeedTrendAnalysis);
        CurveBoundaryExtension maneuverEnd = findSpeedMaximum(stepsToAnalyze, false, timePointBeforeLocalMaximumSearch);
        if (checkCourseChangeLimitExceededForCurveExtension(maneuverMainCurveDetails, maneuverEnd)) {
            maneuverEnd = null;
        }
        TimePoint stableBearingAnalysisFrom = maneuverEnd == null ? maneuverMainCurveDetails.getTimePointAfter()
                : maneuverEnd.getExtensionTimePoint();
        double courseChangeSinceManeuverMainCurveInDegrees = maneuverEnd == null ? 0
                : maneuverEnd.getCourseChangeInDegreesWithinExtensionArea();
        stepsToAnalyze = getSpeedWithBearingStepsWithinTimeRange(stepsToAnalyze, stableBearingAnalysisFrom,
                latestTimePointForSpeedTrendAnalysis);
        CurveBoundaryExtension stableBearingExtension = findStableBearingWithMaxAbsCourseChangeSpeed(stepsToAnalyze,
                false, MAX_ABS_COURSE_CHANGE_IN_DEGREES_PER_SECOND_FOR_STABLE_BEARING_ANALYSIS);
        if (stableBearingExtension != null
                && !checkCourseChangeLimitExceededForCurveExtension(maneuverMainCurveDetails, stableBearingExtension)) {
            maneuverEnd = stableBearingExtension;
            courseChangeSinceManeuverMainCurveInDegrees += stableBearingExtension
                    .getCourseChangeInDegreesWithinExtensionArea();
        }
        return maneuverEnd != null ? new CurveBoundaryExtension(maneuverEnd.getExtensionTimePoint(),
                maneuverEnd.getSpeedWithBearingAtExtensionTimePoint(), courseChangeSinceManeuverMainCurveInDegrees)
                : new CurveBoundaryExtension(maneuverMainCurveDetails.getTimePointAfter(),
                        maneuverMainCurveDetails.getSpeedWithBearingAfter(), 0);
    }

    /**
     * Finds speed maximum considering the provided {@code stepsToAnalyze}. In order to limit the time range for the
     * speed maximum search, the caller must cut off the appropriate steps from the provided {@code stepsToAnalyze}.
     * Additionally, the method supports specification of {@code globalMaximumSearchUntilTimePoint} which defines the
     * time point since which the search is supposed to continue only if the speed continues to rise.
     * 
     * @param stepsToAnalyze
     *            Steps which are used for speed maximum search. Must be in chronological order (forward in time).
     * @param timeBackwardSearch
     *            {@code true} if the search should be performed backwards in time, {@code false} for forward in time.
     *            When the search is performed backwards in time, then the provided {@code stepsToAnalyze} are going to
     *            be iterated in the reverse order.
     * @param globalMaximumSearchUntilTimePoint
     *            The time point after which the search iteration is going to continue only if the speed continues to
     *            rise. {@code null} will deactivate this feature.
     * @return The time point and speed at located step with speed maximum, as well as the total course change from the
     *         step iteration started until the step with the speed maximum
     */
    public CurveBoundaryExtension findSpeedMaximum(SpeedWithBearingStepsIterable stepsToAnalyze,
            boolean timeBackwardSearch, TimePoint globalMaximumSearchUntilTimePoint) {
        final Iterable<SpeedWithBearingStep> finalStepsToAnalyze;
        final Predicate<SpeedWithBearingStep> localMaximumSearch;
        if (timeBackwardSearch) {
            // reverse the steps to iterate through
            finalStepsToAnalyze = cloneAndReverseIterable(stepsToAnalyze);
            localMaximumSearch = step -> globalMaximumSearchUntilTimePoint == null ? false
                    : step.getTimePoint().before(globalMaximumSearchUntilTimePoint);
        } else {
            finalStepsToAnalyze = stepsToAnalyze;
            localMaximumSearch = step -> globalMaximumSearchUntilTimePoint == null ? false
                    : step.getTimePoint().after(globalMaximumSearchUntilTimePoint);
        }

        double previousSpeedInKnots = 0;
        double maxSpeedInKnots = 0;
        SpeedWithBearingStep stepWithMaxSpeed = null;
        double courseChangeSinceMainCurveBeforeSpeedMaximumInDegrees = 0;
        double courseChangeAfterStepWithSpeedMaximum = 0;

        for (SpeedWithBearingStep speedWithBearingStep : finalStepsToAnalyze) {
            courseChangeAfterStepWithSpeedMaximum += speedWithBearingStep.getCourseChangeInDegrees();
            double speedInKnots = speedWithBearingStep.getSpeedWithBearing().getKnots();
            if (localMaximumSearch.test(speedWithBearingStep) && previousSpeedInKnots > speedInKnots) {
                // We are in the interval where the search for speed maximum is supposed to be only continued, if the
                // speed continues to grow. The speed starts to drop => abort further search
                break;
            } else {
                // Otherwise find the step with the highest speed
                if (maxSpeedInKnots < speedInKnots) {
                    maxSpeedInKnots = speedInKnots;
                    stepWithMaxSpeed = speedWithBearingStep;
                    courseChangeSinceMainCurveBeforeSpeedMaximumInDegrees += courseChangeAfterStepWithSpeedMaximum;
                    courseChangeAfterStepWithSpeedMaximum = 0;
                }
            }
            previousSpeedInKnots = speedInKnots;
        }
        // The course change contained in a speed with bearing step references the bearing difference with its preceding
        // step back in time. We need to remove the added course change from the last step in order to not go further
        // time backward.
        if (timeBackwardSearch && stepWithMaxSpeed != null) {
            courseChangeSinceMainCurveBeforeSpeedMaximumInDegrees -= stepWithMaxSpeed.getCourseChangeInDegrees();
        }
        return stepWithMaxSpeed == null ? null
                : new CurveBoundaryExtension(stepWithMaxSpeed.getTimePoint(), stepWithMaxSpeed.getSpeedWithBearing(),
                        courseChangeSinceMainCurveBeforeSpeedMaximumInDegrees);
    }

    private Iterable<SpeedWithBearingStep> cloneAndReverseIterable(SpeedWithBearingStepsIterable stepsToAnalyze) {
        ArrayList<SpeedWithBearingStep> tempSteps = new ArrayList<>();
        for (SpeedWithBearingStep step : stepsToAnalyze) {
            tempSteps.add(step);
        }
        Collections.reverse(tempSteps);
        return tempSteps;
    }

    /**
     * Finds a first section within the provided {@code stepsToAnalyze} where the bearing starts to change with a
     * maximal rate of {@code maxCourseChangeInDegreesPerSecond}.
     * 
     * @param stepsToAnalyze
     * @param timeBackwardSearch
     * @param maxCourseChangeInDegreesPerSecond
     * @return
     * @param stepsToAnalyze
     *            Steps which are used for stable bearing search. Must be in chronological order (forward in time).
     * @param timeBackwardSearch
     *            {@code true} if the search should be performed backward in time, {@code false} for forward in time.
     *            When the search is performed backward in time, then the provided {@code stepsToAnalyze} are going to
     *            be iterated in the reverse order.
     * @param maxCourseChangeInDegreesPerSecond
     *            Defines the course change rate which is regarded as a stable course
     * @return The time point and speed at located step with the first stable course, as well as the total course change
     *         from the step iteration started until the located step
     */
    public CurveBoundaryExtension findStableBearingWithMaxAbsCourseChangeSpeed(
            SpeedWithBearingStepsIterable stepsToAnalyze, boolean timeBackwardSearch,
            double maxCourseChangeInDegreesPerSecond) {
        final Iterable<SpeedWithBearingStep> finalStepsToAnalyze;
        if (timeBackwardSearch) {
            finalStepsToAnalyze = cloneAndReverseIterable(stepsToAnalyze);
        } else {
            finalStepsToAnalyze = stepsToAnalyze;
        }

        SpeedWithBearingStep previousStep = null;
        SpeedWithBearingStep stepUntilStableBearing = null;
        double courseChangeUntilStepWithStableBearingInDegrees = 0;

        for (SpeedWithBearingStep currentStep : finalStepsToAnalyze) {
            if (previousStep != null) {
                double courseChangePerSecondInDegrees = Math.abs(currentStep.getCourseChangeInDegrees()
                        / previousStep.getTimePoint().until(currentStep.getTimePoint()).asSeconds());
                if (courseChangePerSecondInDegrees <= maxCourseChangeInDegreesPerSecond) {
                    stepUntilStableBearing = timeBackwardSearch ? currentStep : previousStep;
                    break;
                }
            }
            courseChangeUntilStepWithStableBearingInDegrees += currentStep.getCourseChangeInDegrees();
            previousStep = currentStep;
        }
        if (stepUntilStableBearing == null) {
            stepUntilStableBearing = previousStep;
        }
        return stepUntilStableBearing == null ? null
                : new CurveBoundaryExtension(stepUntilStableBearing.getTimePoint(),
                        stepUntilStableBearing.getSpeedWithBearing(), courseChangeUntilStepWithStableBearingInDegrees);
    }

    /**
     * Computes maneuver main curve details, such as entering and exiting time point with speed and bearing, time point
     * with the highest turning rate (maneuver climax) and total course change. The strategy here is to cut away bearing
     * steps from the left and right in order to reach a maximal course change corresponding to the target maneuver
     * direction. Furthermore, the main curve boundaries get additionally shortened if the angular velocity of the
     * corresponding steps appears lower than
     * {@value #MIN_ANGULAR_VELOCITY_FOR_MAIN_CURVE_BOUNDARIES_IN_DEGREES_PER_SECOND} degrees per second.
     * 
     * @param maneuverTimePoint
     *            The computed time point of maneuver
     * @param bearingStepsToAnalyze
     *            The bearing steps contained within maneuver
     * @param maneuverDirection
     *            The nautical direction of the maneuver
     * @return The computed entering and exiting time point with its speeds with bearings, time point of maneuver climax
     *         and total course change for the main curve
     */
    public CurveDetails computeManeuverMainCurve(SpeedWithBearingStepsIterable bearingStepsToAnalyze,
            NauticalSide maneuverDirection) {
        double totalCourseChangeSignum = maneuverDirection == NauticalSide.PORT ? -1 : 1;
        double maxCourseChangeInDegrees = 0;
        double currentCourseChangeInDegrees = 0;
        double maxAngularVelocityInDegreesPerSecond = 0;
        TimePoint maneuverTimePoint = null;
        TimePoint previousTimePoint = null;
        // Refine the time point before and after maneuver by checking whether the total course changed before maneuver
        // time point may be increased or kept unchanged if we cut off bearing steps one by one from the left and right.
        TimePoint refinedTimePointBeforeManeuver = null;
        SpeedWithBearing refinedSpeedWithBearingBeforeManeuver = null;
        TimePoint refinedTimePointAfterManeuver = null;
        SpeedWithBearing refinedSpeedWithBearingAfterManeuver = null;
        boolean angularVelocityMinimumReachedAtMainCurveBeginning = false;
        for (SpeedWithBearingStep entry : bearingStepsToAnalyze) {
            currentCourseChangeInDegrees += entry.getCourseChangeInDegrees();
            TimePoint timePoint = entry.getTimePoint();
            // Check whether the totalCourseChange gets notably better with the added course change of current bearing
            // step, considering the target sign of the course change
            if (maxCourseChangeInDegrees * totalCourseChangeSignum < currentCourseChangeInDegrees
                    * totalCourseChangeSignum
                    && entry.getAngularVelocityInDegreesPerSecond() >= MIN_ANGULAR_VELOCITY_FOR_MAIN_CURVE_BOUNDARIES_IN_DEGREES_PER_SECOND) {
                maxCourseChangeInDegrees = currentCourseChangeInDegrees;
                refinedTimePointAfterManeuver = timePoint;
                refinedSpeedWithBearingAfterManeuver = entry.getSpeedWithBearing();
            }
            // Check whether the course change is performed in the target direction of maneuver. If yes, check consider
            // the step to locate the maneuver time point with the highest angular velocity within main curve.
            if (0 < currentCourseChangeInDegrees * totalCourseChangeSignum) {
                if (maxAngularVelocityInDegreesPerSecond < entry.getAngularVelocityInDegreesPerSecond()) {
                    maxAngularVelocityInDegreesPerSecond = entry.getAngularVelocityInDegreesPerSecond();
                    Duration durationFromPreviousStep = previousTimePoint.until(timePoint);
                    maneuverTimePoint = previousTimePoint.plus(durationFromPreviousStep.divide(2.0));
                }
            }
            // If the direction sign does not match, or the angular velocity at the beginning of the curve is nearly
            // zero => cut the bearing step from the left
            if (0 >= currentCourseChangeInDegrees * totalCourseChangeSignum
                    || !angularVelocityMinimumReachedAtMainCurveBeginning && entry
                            .getAngularVelocityInDegreesPerSecond() < MIN_ANGULAR_VELOCITY_FOR_MAIN_CURVE_BOUNDARIES_IN_DEGREES_PER_SECOND) {
                currentCourseChangeInDegrees = 0;
                maxCourseChangeInDegrees = 0;
                refinedTimePointBeforeManeuver = timePoint;
                refinedSpeedWithBearingBeforeManeuver = entry.getSpeedWithBearing();
                refinedTimePointAfterManeuver = null;
                refinedSpeedWithBearingAfterManeuver = null;
                angularVelocityMinimumReachedAtMainCurveBeginning = false;
                maneuverTimePoint = null;
                maxAngularVelocityInDegreesPerSecond = 0;
            } else {
                angularVelocityMinimumReachedAtMainCurveBeginning = true;
            }
            previousTimePoint = timePoint;
        }
        if (refinedTimePointBeforeManeuver == null) {
            // Should not occur, if bearingStepsToAnalyze.size() > 0 and first BearingStep.getCourseChangeInDegrees() ==
            // 0
            return null;
        }
        if (refinedSpeedWithBearingAfterManeuver == null) {
            // Can only occur, when after maneuver time point different direction compared to the analyzed maneuver is
            // sailed. Thus, the resulting time point until the cut operation should be performed is the maneuver time
            // point itself.
            return null;
        }
        CurveDetails maneuverEnteringAndExitingDetails = new CurveDetails(refinedTimePointBeforeManeuver,
                refinedTimePointAfterManeuver, maneuverTimePoint, refinedSpeedWithBearingBeforeManeuver,
                refinedSpeedWithBearingAfterManeuver, maxCourseChangeInDegrees, maxAngularVelocityInDegreesPerSecond);
        return maneuverEnteringAndExitingDetails;
    }

    /**
     * Gets a new list with bearing steps which are lying between provided time range (including the boundaries). To get
     * the steps, only the provided {@code bearingStepsToAnalyze} is processed and filtered accordingly. No calls to
     * {@link GPSFixTrack#getSpeedWithBearingSteps(TimePoint, TimePoint, Duration)} are made.
     */
    public SpeedWithBearingStepsIterable getSpeedWithBearingStepsWithinTimeRange(
            SpeedWithBearingStepsIterable bearingStepsToAnalyze, TimePoint timePointBefore, TimePoint timePointAfter) {
        List<SpeedWithBearingStep> maneuverBearingSteps = new ArrayList<>();
        for (SpeedWithBearingStep entry : bearingStepsToAnalyze) {
            if (entry.getTimePoint().after(timePointAfter)) {
                break;
            }
            if (!entry.getTimePoint().before(timePointBefore)) {
                if (maneuverBearingSteps.isEmpty()) {
                    // First bearing step supposed to have 0 as course change as
                    // it does not have any previous steps with bearings to compute bearing difference.
                    // If the condition is not met, the existing code which uses ManeuverBearingStep class will break.
                    entry = new SpeedWithBearingStepImpl(entry.getTimePoint(), entry.getSpeedWithBearing(), 0.0, 0.0);
                }
                maneuverBearingSteps.add(entry);
            }
        }
        return new SpeedWithBearingStepsIterable(maneuverBearingSteps);
    }

    /**
     * Gets the approximated duration of the maneuver main curve considering the boat class of the competitor.
     */
    private Duration getApproximateManeuverDuration(Competitor competitor) {
        return competitor.getBoat().getBoatClass().getApproximateManeuverDuration();
    }

}

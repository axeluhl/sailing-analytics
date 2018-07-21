package com.sap.sailing.domain.maneuverdetection.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.maneuverdetection.ApproximatedFixesCalculator;
import com.sap.sailing.domain.maneuverdetection.IncrementalManeuverDetector;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.CompleteManeuverCurve;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

/**
 * Incremental maneuver detector, which is capable of detecting maneuvers by a {@link #detectManeuvers()} call in an
 * incremental way. This detector is using {@link ApproximatedFixesCalculatorImpl} for calculation of Douglas Peucker
 * fixes. When the Douglas Peucker fixes are calculated, it tries to match the resulting fixes with already calculated
 * Douglas Peucker fixes groups for already calculated maneuvers (represented by {@link ManeuverSpot}) from previous
 * {@link #detectManeuvers()} calls. The existing maneuvers of matched existing Douglas Peucker fixes groups are reused,
 * when the following conditions are met:
 * <ul>
 * <li>The next determined Douglas Peucker fix following after the last fix of the matched existing Douglas Peucker
 * fixes group gets matched with the beginning fix of an another existing Douglas Peucker fixes group</li>
 * <li>The currently measured wind within the Douglas Peucker fixes group is nearly the same as previouly measured when
 * the existing Douglas Peucker fixes group had been determined.</li>
 * <li>The reused Douglas Peucker fixes group is far enough from the last fix of the track, so that its maneuvers cannot
 * be extended by new incoming fixes. The "far enough" is defined in
 * {@link #checkManeuverSpotFarEnoughFromLatestRawFix(TimePoint, long, TimePoint, ManeuverSpot)}.
 * </ul>
 * With exception: the first and last calculated Douglas Peucker points are ignored during matching process, because
 * they get never added to Douglas Peucker fixes sets which represent maneuver section.
 * {@link #checkDouglasPeuckerFixesNearlySame(GPSFixMoving, GPSFixMoving)} defines whether two Douglas Peucker match and
 * are nearly same. The nearly the same measured wind is defined by
 * {@link #checkManeuverSpotWindNearlySame(ManeuverSpot)}.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class IncrementalManeuverDetectorImpl extends ManeuverDetectorImpl implements IncrementalManeuverDetector {

    /**
     * The wind course tolerance in degrees which defines the maximal acceptable deviation of wind measurement between
     * previously measured wind and the current recorded wind at the same maneuver spot. If the tolerance limit is
     * exceeded, the maneuvers for the analysed maneuver spot get recalculated.
     */
    private static final double WIND_COURSE_TOLERANCE_IN_DEGREES_TO_IGNORE_FOR_MANEUVER_REUSE = 5.0;

    /**
     * Defines the tolerance in seconds between last time points of douglas peucker fixes within a maneuver spot and the
     * time points of currently analysed douglas peucker fixes. If the tolerance limit is exceeded, the existing
     * maneuver spot gets discarded.
     */
    private static final double DOUGLAS_PEUCKER_FIXES_TIME_POINT_TOLERANCE_IN_SECONDS_TO_IGNORE_FOR_MANEUVER_REUSE = 1;

    /**
     * The result of previous maneuver detection, or {@code null} if not performed until yet
     */
    private volatile ManeuverDetectionResult lastManeuverDetectionResult = null;

    /**
     * Calculator for douglas peucker fixes
     */
    private final ApproximatedFixesCalculator approximatedFixesCalculator;

    /**
     * Constructor for unit tests only.
     */
    public IncrementalManeuverDetectorImpl() {
        super();
        this.approximatedFixesCalculator = null;
    }

    /**
     * Constructs incremental maneuver detector which is supposed to be used for maneuver detection within the provided
     * tracked race for provided competitor.
     * 
     * @param trackedRace
     *            The tracked race whose maneuvers are supposed to be detected
     * @param competitor
     *            The competitor, whose maneuvers shall be discovered
     */
    public IncrementalManeuverDetectorImpl(TrackedRace trackedRace, Competitor competitor) {
        super(trackedRace, competitor);
        // TODO Use IncrementalApproximatedFixesCalculatorImpl when its deprecation status gets fixed
        this.approximatedFixesCalculator = new ApproximatedFixesCalculatorImpl(trackedRace, competitor);
    }

    @Override
    public List<Maneuver> getAlreadyDetectedManeuvers() {
        ManeuverDetectionResult lastManeuverDetectionResult = this.lastManeuverDetectionResult;
        if (lastManeuverDetectionResult != null) {
            return getAllManeuversFromManeuverSpots(lastManeuverDetectionResult.getManeuverSpots());
        }
        return Collections.emptyList();
    }
    
    @Override
    public List<CompleteManeuverCurve> getAlreadyDetectedManeuverCurves() {
        ManeuverDetectionResult lastManeuverDetectionResult = this.lastManeuverDetectionResult;
        if (lastManeuverDetectionResult != null) {
            return lastManeuverDetectionResult.getManeuverSpots().stream().filter(maneuverSpot -> maneuverSpot.getManeuverCurve() != null)
                    .map(maneuverSpot -> maneuverSpot.getManeuverCurve()).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public void clearState() {
        lastManeuverDetectionResult = null;
        // TODO If IncrementalApproximatedFixesCalculator is used, call its
        // incrementalApproximatedFixesCalculator.clearState() also.
    }

    @Override
    protected List<ManeuverSpot> detectManeuverSpots() {
        TrackTimeInfo trackTimeInfo = getTrackTimeInfo();
        if (trackTimeInfo != null) {
            TimePoint earliestManeuverStart = trackTimeInfo.getTrackStartTimePoint();
            TimePoint latestManeuverEnd = trackTimeInfo.getTrackEndTimePoint();
            TimePoint latestRawFixTimePoint = trackTimeInfo.getLatestRawFixTimePoint();
            Iterable<GPSFixMoving> douglasPeuckerFixes = approximatedFixesCalculator.approximate(earliestManeuverStart,
                    latestManeuverEnd);
            ManeuverDetectionResult lastManeuverDetectionResult = this.lastManeuverDetectionResult;
            List<ManeuverSpot> maneuverSpots;
            if (lastManeuverDetectionResult == null) {
                maneuverSpots = detectManeuvers(douglasPeuckerFixes, earliestManeuverStart, latestManeuverEnd);
            } else {
                maneuverSpots = detectManeuversIncrementally(trackTimeInfo, douglasPeuckerFixes,
                        lastManeuverDetectionResult);
            }
            int incrementalRunsCount = lastManeuverDetectionResult == null ? 1
                    : (lastManeuverDetectionResult.getIncrementalRunsCount() < Integer.MAX_VALUE
                            ? lastManeuverDetectionResult.getIncrementalRunsCount() + 1 : Integer.MAX_VALUE);
            this.lastManeuverDetectionResult = new ManeuverDetectionResult(latestRawFixTimePoint, maneuverSpots,
                    incrementalRunsCount);
            return maneuverSpots;
        }
        return Collections.emptyList();
    }

    // public for unit tests
    public List<ManeuverSpot> detectManeuversIncrementally(TrackTimeInfo trackTimeInfo,
            Iterable<GPSFixMoving> approximatingFixesToAnalyze, ManeuverDetectionResult lastManeuverDetectionResult) {
        TimePoint earliestManeuverStart = trackTimeInfo.getTrackStartTimePoint();
        TimePoint latestManeuverEnd = trackTimeInfo.getTrackEndTimePoint();
        TimePoint latestRawFixTimePoint = trackTimeInfo.getLatestRawFixTimePoint();
        long maxDurationForDouglasPeuckerFixExtensionInManeuverAnalysisInMillis = getMaxDurationForDouglasPeuckerFixExtensionInManeuverAnalysis()
                .asMillis();
        TimePoint latestRawFixTimePointOfPreviousManeuverDetectionIteration = lastManeuverDetectionResult
                .getLatestRawFixTimePoint();
        List<ManeuverSpot> result = new ArrayList<>();
        if (Util.size(approximatingFixesToAnalyze) > 2) {
            List<GPSFixMoving> fixesGroupForManeuverSpotAnalysis = new ArrayList<GPSFixMoving>();
            Iterator<GPSFixMoving> approximationPointsIter = approximatingFixesToAnalyze.iterator();
            GPSFixMoving previous = approximationPointsIter.next();
            GPSFixMoving current = approximationPointsIter.next();
            NauticalSide lastCourseChangeDirection = null;
            ManeuverSpot matchingManeuverSpotFromState = null;
            Iterator<GPSFixMoving> matchingFixesGroupFromStateIterator = null;
            ListIterator<ManeuverSpot> lastManeuverSpotIteratorUsed = getExistingManeuverSpotByFirstDouglasPeuckerFix(
                    lastManeuverDetectionResult, null, current);
            ManeuverSpot nextExistingSpot = lastManeuverSpotIteratorUsed != null ? lastManeuverSpotIteratorUsed.next()
                    : null;
            do {
                GPSFixMoving next = approximationPointsIter.next();
                // check if we have previously found a similar fixes group from state
                if (matchingManeuverSpotFromState != null) {
                    if (matchingFixesGroupFromStateIterator.hasNext()) {
                        GPSFixMoving existingDouglasPeuckerFix = matchingFixesGroupFromStateIterator.next();
                        if (!checkDouglasPeuckerFixesNearlySame(existingDouglasPeuckerFix, current)) {
                            // existing maneuver spot does not match with the fixes sequence in this run => discard
                            // existing maneuver spot and process fixesGroupForManeuverSpotAnalysis normally like in
                            // ManeuverDetectorImpl
                            matchingManeuverSpotFromState = null;
                        }
                    } else {
                        // check if the existing group is followed by an existing group, otherwise discard the existing
                        // maneuver spot, because it can possibly be extended by the next fix.
                        ListIterator<ManeuverSpot> maneuverSpotIterator = getExistingManeuverSpotByFirstDouglasPeuckerFix(
                                lastManeuverDetectionResult, lastManeuverSpotIteratorUsed, current);
                        if (maneuverSpotIterator != null) {
                            if (maneuverSpotIterator != null) {
                                nextExistingSpot = maneuverSpotIterator.next();
                                lastManeuverSpotIteratorUsed = maneuverSpotIterator;
                            }
                            if (checkManeuverSpotWindNearlySame(matchingManeuverSpotFromState)) {
                                // We found an existing maneuver spot with similar fixes and estimated winds => reuse
                                // existing maneuver spot
                                result.add(matchingManeuverSpotFromState);
                            } else {
                                // New wind information has been received which considerably differs from previous
                                // maneuver spot calculation => recalculate maneuvers of existing maneuver curve
                                CompleteManeuverCurve maneuverCurve = matchingManeuverSpotFromState.getManeuverCurve();
                                ManeuverSpot maneuverSpot;
                                if (maneuverCurve != null) {
                                    WindMeasurement windMeasurement = matchingManeuverSpotFromState
                                            .getWindMeasurement();
                                    Wind wind = trackedRace.getWind(windMeasurement.getPosition(),
                                            windMeasurement.getTimePoint());
                                    List<Maneuver> maneuvers = determineManeuversFromManeuverCurve(
                                            maneuverCurve.getMainCurveBoundaries(),
                                            maneuverCurve.getManeuverCurveWithStableSpeedAndCourseBoundaries(), wind,
                                            maneuverCurve.getMarkPassing());
                                    maneuverSpot = new ManeuverSpot(
                                            matchingManeuverSpotFromState.getDouglasPeuckerFixes(),
                                            matchingManeuverSpotFromState.getManeuverSpotDirection(), maneuverCurve,
                                            maneuvers, new WindMeasurement(windMeasurement.getTimePoint(),
                                                    windMeasurement.getPosition(), wind.getBearing()));
                                } else {
                                    maneuverSpot = matchingManeuverSpotFromState;
                                }
                                result.add(maneuverSpot);
                            }
                            fixesGroupForManeuverSpotAnalysis.clear();
                        }
                        matchingManeuverSpotFromState = null;
                    }
                }
                // If we are not matching the fixes with existing fixes group, analyze fixes grouping normally like
                // ManeuverDetectorImpl does
                if (matchingManeuverSpotFromState == null && nextExistingSpot == null) {
                    // Split douglas peucker fixes groups to identify maneuver spots
                    NauticalSide courseChangeDirectionOnOriginalFixes = getCourseChangeDirectionAroundFix(
                            previous.getTimePoint(), current, next.getTimePoint());
                    if (!fixesGroupForManeuverSpotAnalysis.isEmpty()
                            && !checkDouglasPeuckerFixesGroupable(lastCourseChangeDirection,
                                    courseChangeDirectionOnOriginalFixes, previous, current)) {
                        // current fix does not belong to the existing fixes group; determine maneuvers of recent fixes
                        // group, then start a new list
                        ManeuverSpot maneuverSpot = createManeuverSpotWithManeuversFromFixesGroup(
                                fixesGroupForManeuverSpotAnalysis, lastCourseChangeDirection, earliestManeuverStart,
                                latestManeuverEnd);
                        result.add(maneuverSpot);
                        fixesGroupForManeuverSpotAnalysis.clear();
                    }
                    lastCourseChangeDirection = courseChangeDirectionOnOriginalFixes;
                }
                fixesGroupForManeuverSpotAnalysis.add(current);
                // check if we have a new fixes group.
                if (fixesGroupForManeuverSpotAnalysis.size() == 1) {
                    // Check if we got an existing maneuver spot with similar fix at beginning
                    ManeuverSpot maneuverSpot;
                    if (nextExistingSpot != null) {
                        maneuverSpot = nextExistingSpot;
                        nextExistingSpot = null;
                    } else {
                        ListIterator<ManeuverSpot> maneuverSpotIterator = getExistingManeuverSpotByFirstDouglasPeuckerFix(
                                lastManeuverDetectionResult, lastManeuverSpotIteratorUsed, current);
                        if (maneuverSpotIterator != null) {
                            maneuverSpot = maneuverSpotIterator.next();
                            lastManeuverSpotIteratorUsed = maneuverSpotIterator;
                        } else {
                            maneuverSpot = null;
                        }
                    }
                    if (maneuverSpot != null) {
                        // if the maneuver
                        // spot is lying within time range of latestRawFix.getTimePoint() - (longest maneuver
                        // duration)
                        // and
                        // latestRawFixTimePoint.after(latestRawFixTimePointOfPreviousManeuverDetectionIteration),
                        // then we need to recalculate the maneuver spot, because the boundaries of maneuver may get
                        // extended by new incoming fixes
                        boolean maneuverSpotIsFarEnoughFromLatestRawFix = checkManeuverSpotFarEnoughFromLatestRawFix(
                                latestRawFixTimePoint,
                                maxDurationForDouglasPeuckerFixExtensionInManeuverAnalysisInMillis,
                                latestRawFixTimePointOfPreviousManeuverDetectionIteration, maneuverSpot);

                        if (maneuverSpotIsFarEnoughFromLatestRawFix) {
                            matchingManeuverSpotFromState = maneuverSpot;
                            matchingFixesGroupFromStateIterator = maneuverSpot.getDouglasPeuckerFixes().iterator();
                            // first fix already matched with getExistingManeuverSpotByFirstDouglasPeuckerFix(current)
                            // call => move iteration cursor to next
                            matchingFixesGroupFromStateIterator.next();
                        }
                        lastCourseChangeDirection = maneuverSpot.getManeuverSpotDirection();
                    }
                }
                previous = current;
                current = next;
            } while (approximationPointsIter.hasNext());
            if (!fixesGroupForManeuverSpotAnalysis.isEmpty()) {
                ManeuverSpot maneuverSpot = createManeuverSpotWithManeuversFromFixesGroup(
                        fixesGroupForManeuverSpotAnalysis, lastCourseChangeDirection, earliestManeuverStart,
                        latestManeuverEnd);
                result.add(maneuverSpot);
            }
        }
        return result;
    }

    /**
     * Checks whether the provided maneuver spot is too close to {@code latestRawFixTimePoint}, so that recalculation of
     * the maneuver boundaries is needed, because the boundaries may get extended by new incoming fixes.
     */
    private boolean checkManeuverSpotFarEnoughFromLatestRawFix(TimePoint latestRawFixTimePoint,
            long maxDurationForDouglasPeuckerFixExtensionInManeuverAnalysisInMillis,
            TimePoint latestRawFixTimePointOfPreviousManeuverDetectionIteration,
            ManeuverSpot previouslyDetectedManeuverSpotWithSameDouglasPeuckerPoints) {
        if (latestRawFixTimePoint.after(latestRawFixTimePointOfPreviousManeuverDetectionIteration)) {
            GPSFixMoving latestDouglasPeuckerFix = null;
            for (GPSFixMoving fix : previouslyDetectedManeuverSpotWithSameDouglasPeuckerPoints
                    .getDouglasPeuckerFixes()) {
                latestDouglasPeuckerFix = fix;
            }
            if (latestDouglasPeuckerFix.getTimePoint().until(latestRawFixTimePoint)
                    .asMillis() < maxDurationForDouglasPeuckerFixExtensionInManeuverAnalysisInMillis) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the max duration by which the maneuver boundary can be extended after the latest douglas peucker fix of
     * provided maneuver spot.
     */
    private Duration getMaxDurationForDouglasPeuckerFixExtensionInManeuverAnalysis() {
        Duration approximateManeuverDuration = getApproximateManeuverDuration();
        return getDurationForDouglasPeuckerExtensionForMainCurveAnalysis(approximateManeuverDuration)
                .plus(getMaxDurationForAfterManeuverSectionExtension(approximateManeuverDuration));
    }

    private boolean checkManeuverSpotWindNearlySame(ManeuverSpot maneuverSpot) {
        if (maneuverSpot.getWindMeasurement() != null) {
            WindMeasurement windMeasurement = maneuverSpot.getWindMeasurement();
            Bearing lastWindCourse = windMeasurement.getWindCourse();
            Wind currentWind = trackedRace.getWind(windMeasurement.getPosition(), windMeasurement.getTimePoint());
            if (lastWindCourse == null && currentWind == null) {
                return true;
            }
            if (lastWindCourse == null || currentWind == null) {
                return false;
            }
            double bearingInDegrees = lastWindCourse.getDifferenceTo(currentWind.getBearing()).getDegrees();
            if (bearingInDegrees > WIND_COURSE_TOLERANCE_IN_DEGREES_TO_IGNORE_FOR_MANEUVER_REUSE) {
                return false;
            }
        }
        // maneuver spot has no maneuvers, or previously measured wind and current wind are within tolerance level
        return true;
    }

    private boolean checkDouglasPeuckerFixesNearlySame(GPSFixMoving existingDouglasPeuckerFix,
            GPSFixMoving newDouglasPeuckerFix) {
        double secondsDifference = existingDouglasPeuckerFix.getTimePoint().until(newDouglasPeuckerFix.getTimePoint())
                .asSeconds();
        if (Math.abs(
                secondsDifference) > DOUGLAS_PEUCKER_FIXES_TIME_POINT_TOLERANCE_IN_SECONDS_TO_IGNORE_FOR_MANEUVER_REUSE) {
            return false;
        }
        return true;
    }

    // for unit tests only
    public void setLastManeuverDetectionResult(ManeuverDetectionResult lastManeuverDetectionResult) {
        this.lastManeuverDetectionResult = lastManeuverDetectionResult;
    }

    @Override
    public int getIncrementalRunsCount() {
        ManeuverDetectionResult lastManeuverDetectionResult = this.lastManeuverDetectionResult;
        return lastManeuverDetectionResult != null ? lastManeuverDetectionResult.getIncrementalRunsCount() : 0;
    }

    /**
     * Tries to get an already processed maneuver spot from previous calls of {@link #detectManeuvers()} which starts
     * with a douglas peucker fix similar to the provided {@code newDouglasPeuckerFix}. This method was designed to run
     * within loops of {@link #detectManeuversIncrementally(TrackTimeInfo, Iterable, ManeuverDetectionResult)}. In order
     * to prevent squared iteration complexity, the method returns a {@code ListIterator} which is supposed to be used
     * to retrieve the located existing maneuver spot, as well as to provide the same iterator for the following call of
     * this method within following iterations, in order to resume the search iteration from the position of the
     * previously retrieved maneuver spot.
     * 
     * @param lastManeuverDetectionResult
     *            The result of previously performed maneuver detection, which contains the maneuver spots to look up
     * @param lastIteratorUsed
     *            {@code ListIterator}, which was returned by previous call of this method, or {@code null} which causes
     *            iteration of existing maneuver spots start from scratch
     * @param newDouglasPeuckerFix
     *            The beginning douglas peucker fix of maneuver spot to find
     * @return {@code null} if no corresponding maneuver spot could be found, otherwise a {@code ListIterator} which
     *         points to the matched maneuver spot in its first {@code next()} call.
     */
    private ListIterator<ManeuverSpot> getExistingManeuverSpotByFirstDouglasPeuckerFix(
            ManeuverDetectionResult lastManeuverDetectionResult, ListIterator<ManeuverSpot> lastIteratorUsed,
            GPSFixMoving newDouglasPeuckerFix) {
        ManeuverSpot firstManeuverSpotIterated = null;
        if (lastIteratorUsed != null) {
            while (lastIteratorUsed.hasNext()) {
                ManeuverSpot maneuverSpot = lastIteratorUsed.next();
                if (firstManeuverSpotIterated == null) {
                    firstManeuverSpotIterated = maneuverSpot;
                }
                GPSFixMoving firstFix = maneuverSpot.getDouglasPeuckerFixes().iterator().next();
                if (checkDouglasPeuckerFixesNearlySame(newDouglasPeuckerFix, firstFix)) {
                    lastIteratorUsed.previous();
                    return lastIteratorUsed;
                }
            }
        }
        ListIterator<ManeuverSpot> newIterator = lastManeuverDetectionResult.getManeuverSpots().listIterator();
        // no maneuver spot detected with lastIteratorUsed. Try from beginning until firstManeuverSpotIterated
        while (newIterator.hasNext()) {
            ManeuverSpot maneuverSpot = newIterator.next();
            if (maneuverSpot == firstManeuverSpotIterated) {
                break;
            }
            GPSFixMoving firstFix = maneuverSpot.getDouglasPeuckerFixes().iterator().next();
            if (checkDouglasPeuckerFixesNearlySame(newDouglasPeuckerFix, firstFix)) {
                newIterator.previous();
                return newIterator;
            }
        }

        return null;
    }

    // public for unit tests
    /**
     * Represents a result of already performed maneuver analysis. The result is used by
     * {@link IncrementalManeuverDetectorImpl} to determine maneuvers incrementally.
     * 
     * @author Vladislav Chumak (D069712)
     *
     */
    public static class ManeuverDetectionResult {

        private final TimePoint latestFixTimePoint;
        private final List<ManeuverSpot> maneuverSpots;
        private final int incrementalRunsCount;

        public ManeuverDetectionResult(TimePoint latestFixTimePoint, List<ManeuverSpot> maneuverSpots,
                int incrementalRunsCount) {
            this.latestFixTimePoint = latestFixTimePoint;
            this.maneuverSpots = maneuverSpots;
            this.incrementalRunsCount = incrementalRunsCount;
        }

        public TimePoint getLatestRawFixTimePoint() {
            return latestFixTimePoint;
        }

        public List<ManeuverSpot> getManeuverSpots() {
            return maneuverSpots;
        }

        public int getIncrementalRunsCount() {
            return incrementalRunsCount;
        }

    }

}

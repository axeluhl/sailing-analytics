package com.sap.sailing.domain.maneuverdetection.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.maneuverdetection.IncrementalManeuverDetector;
import com.sap.sailing.domain.maneuverdetection.NoFixesException;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

public class IncrementalManeuverDetectorImpl extends ManeuverDetectorImpl implements IncrementalManeuverDetector {

    private static final double WIND_TOLERANCE_TO_IGNORE_FOR_MANEUVER_REUSE = 5.0;
    private static final double DOUGLAS_PEUCKER_FIXES_TIME_POINT_TOLERANCE_TO_IGNORE_FOR_MANEUVER_REUSE = 1;
    private Map<Competitor, ManeuverDetectionResult> existingManeuverSpotsPerCompetitor = new ConcurrentHashMap<>();

    public IncrementalManeuverDetectorImpl(TrackedRace trackedRace) {
        super(trackedRace);
    }

    @Override
    public List<Maneuver> getAlreadyDetectedManeuvers(Competitor competitor) {
        ManeuverDetectionResult lastManeuverDetectionResult = existingManeuverSpotsPerCompetitor.get(competitor);
        if (lastManeuverDetectionResult != null) {
            return getAllManeuversFromManeuverSpots(lastManeuverDetectionResult.getManeuverSpots());
        }
        return Collections.emptyList();
    }

    @Override
    public void clearState(Competitor competitor) {
        existingManeuverSpotsPerCompetitor.remove(competitor);
    }

    @Override
    public void clearState() {
        existingManeuverSpotsPerCompetitor.clear();
    }

    @Override
    public List<Maneuver> detectManeuvers(Competitor competitor) throws NoWindException, NoFixesException {
        // TODO approximated points need to have previous, current and next
        // TODO iteratedButNotAnalysedPoints handling
        // TODO Caching von ManeuverDetector attribut in TrackedRaceImpl?
        // TODO Tests
        TrackTimeInfo trackTimeInfo = getTrackingStartAndEndTimePoints(competitor);
        if (trackTimeInfo != null) {
            TimePoint earliestManeuverStart = trackTimeInfo.getTrackStartTimePoint();
            TimePoint latestManeuverEnd = trackTimeInfo.getTrackEndTimePoint();
            TimePoint latestRawFixTimePoint = trackTimeInfo.getLatestRawFixTimePoint();
            Iterable<GPSFixMoving> douglasPeuckerFixes = trackedRace.approximate(competitor,
                    competitor.getBoat().getBoatClass().getMaximumDistanceForCourseApproximation(),
                    earliestManeuverStart, latestManeuverEnd);
            ManeuverDetectionResult lastManeuverDetectionResult = existingManeuverSpotsPerCompetitor.get(competitor);
            List<ManeuverSpot> maneuverSpots;
            if (lastManeuverDetectionResult == null) {
                maneuverSpots = detectManeuvers(competitor, douglasPeuckerFixes, earliestManeuverStart,
                        latestManeuverEnd);
            } else {
                maneuverSpots = detectManeuversIncrementally(competitor, trackTimeInfo, douglasPeuckerFixes,
                        lastManeuverDetectionResult);
            }
            existingManeuverSpotsPerCompetitor.put(competitor,
                    new ManeuverDetectionResult(latestRawFixTimePoint, maneuverSpots));
            return getAllManeuversFromManeuverSpots(maneuverSpots);
        }
        throw new NoFixesException();
    }

    private List<ManeuverSpot> detectManeuversIncrementally(Competitor competitor, TrackTimeInfo trackTimeInfo,
            Iterable<GPSFixMoving> approximatingFixesToAnalyze, ManeuverDetectionResult lastManeuverDetectionResult)
            throws NoWindException {
        TimePoint earliestManeuverStart = trackTimeInfo.getTrackStartTimePoint();
        TimePoint latestManeuverEnd = trackTimeInfo.getTrackEndTimePoint();
        TimePoint latestRawFixTimePoint = trackTimeInfo.getLatestRawFixTimePoint();
        long maxDurationForDouglasPeuckerFixExtensionInManeuverAnalysisInMillis = getMaxDurationForDouglasPeuckerFixExtensionInManeuverAnalysis(
                competitor).asMillis();
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
            ListIterator<ManeuverSpot> lastManeuverSpotIteratorUsed = null;
            do {
                GPSFixMoving next = approximationPointsIter.next();
                ManeuverSpot nextExistingSpot = null;
                // check if we have previously found a similar fixes group from state
                if (matchingManeuverSpotFromState != null) {
                    boolean resetMatchingFixesGroup = false;
                    if (matchingFixesGroupFromStateIterator.hasNext()) {
                        GPSFixMoving existingDouglasPeuckerFix = matchingFixesGroupFromStateIterator.next();
                        if (!checkDouglasPeuckerFixesNearlySame(existingDouglasPeuckerFix, current)) {
                            // existing maneuver spot does not match with the fixes sequence in this run => discard
                            // existing maneuver spot and process fixesGroupForManeuverSpotAnalysis normally like in
                            // ManeuverDetectorImpl
                            resetMatchingFixesGroup = true;
                        }
                    } else {
                        // check if the existing group is followed by an existing group, otherwise discard the existing
                        // maneuver spot, because it can possibly be extended by the next fix. Ignore this check if next
                        // fix is the last fix. First and last fix never get to a maneuver spot.
                        ListIterator<ManeuverSpot> maneuverSpotIterator = getExistingManeuverSpotByFirstDouglasPeuckerFix(
                                lastManeuverDetectionResult, lastManeuverSpotIteratorUsed, current);
                        if (maneuverSpotIterator != null || !approximationPointsIter.hasNext()) {
                            nextExistingSpot = maneuverSpotIterator.next();
                            lastManeuverSpotIteratorUsed = maneuverSpotIterator;
                            if (checkManeuverSpotWindNearlySame(matchingManeuverSpotFromState)) {
                                // We found an existing maneuver spot with similar fixes and estimated winds => reuse
                                // existing maneuver spot
                                result.add(matchingManeuverSpotFromState);
                            } else {
                                // New wind information has been received which considerably differs from previous
                                // maneuver spot calculation => recalculate existing maneuver spot maneuvers
                                ManeuverSpot maneuverSpot = createManeuverFromFixesGroup(competitor,
                                        fixesGroupForManeuverSpotAnalysis,
                                        matchingManeuverSpotFromState.getManeuverSpotDirection(), earliestManeuverStart,
                                        latestManeuverEnd);
                                result.add(maneuverSpot);
                            }
                            fixesGroupForManeuverSpotAnalysis.clear();
                        }
                        resetMatchingFixesGroup = true;
                    }
                    if (resetMatchingFixesGroup) {
                        lastCourseChangeDirection = matchingManeuverSpotFromState.getManeuverSpotDirection();
                        matchingManeuverSpotFromState = null;
                    }
                }
                // If we are not matching the fixes with existing fixes group, analyze fixes grouping normally like
                // ManeuverDetectorImpl does
                if (matchingManeuverSpotFromState == null) {
                    // Split douglas peucker fixes groups to identify maneuver spots
                    NauticalSide courseChangeDirectionOnOriginalFixes = getCourseChangeDirectionAroundFix(competitor,
                            previous.getTimePoint(), current, next.getTimePoint());
                    if (!fixesGroupForManeuverSpotAnalysis.isEmpty() && !checkDouglasPeuckerFixesGroupable(competitor,
                            lastCourseChangeDirection, courseChangeDirectionOnOriginalFixes, previous, current)) {
                        // current fix does not belong to the existing fixes group; determine maneuvers of recent fixes
                        // group, then start a new list
                        ManeuverSpot maneuverSpot = createManeuverFromFixesGroup(competitor,
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
                    }
                }
                previous = current;
                current = next;
            } while (approximationPointsIter.hasNext());
            if (!fixesGroupForManeuverSpotAnalysis.isEmpty()) {
                ManeuverSpot maneuverSpot = createManeuverFromFixesGroup(competitor, fixesGroupForManeuverSpotAnalysis,
                        lastCourseChangeDirection, earliestManeuverStart, latestManeuverEnd);
                result.add(maneuverSpot);
            }
        }
        return result;
    }

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

    private Duration getMaxDurationForDouglasPeuckerFixExtensionInManeuverAnalysis(Competitor competitor) {
        Duration approximateManeuverDuration = getApproximateManeuverDuration(competitor);
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
            if (bearingInDegrees > WIND_TOLERANCE_TO_IGNORE_FOR_MANEUVER_REUSE) {
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
        if (Math.abs(secondsDifference) > DOUGLAS_PEUCKER_FIXES_TIME_POINT_TOLERANCE_TO_IGNORE_FOR_MANEUVER_REUSE) {
            return false;
        }
        return true;
    }

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

}

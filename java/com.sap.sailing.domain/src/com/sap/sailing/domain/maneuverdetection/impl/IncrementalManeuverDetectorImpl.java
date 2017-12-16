package com.sap.sailing.domain.maneuverdetection.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.maneuverdetection.IncrementalManeuverDetector;
import com.sap.sailing.domain.maneuverdetection.NoFixesException;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;

public class IncrementalManeuverDetectorImpl extends ManeuverDetectorImpl implements IncrementalManeuverDetector {

    private Map<Competitor, ManeuverDetectionResult> existingManeuverSpotsPerCompetitor = new ConcurrentHashMap<>();

    public IncrementalManeuverDetectorImpl(TrackedRace trackedRace) {
        super(trackedRace);
    }
    
    public List<Maneuver> getAlreadyDetectedManeuvers(Competitor competitor) {
        ManeuverDetectionResult lastManeuverDetectionResult = existingManeuverSpotsPerCompetitor.get(competitor);
        if(lastManeuverDetectionResult != null) {
            return getAllManeuversFromManeuverSpots(lastManeuverDetectionResult.getManeuverSpots());
        }
        return Collections.emptyList();
    }
    
    public void clearState(Competitor competitor) {
        existingManeuverSpotsPerCompetitor.remove(competitor);
    }
    
    public void clearState() {
        existingManeuverSpotsPerCompetitor.clear();
    }

    @Override
    public List<Maneuver> detectManeuvers(Competitor competitor) throws NoWindException, NoFixesException {
        //TODO approximated points need to have previous, current and next
        //TODO iteratedButNotAnalysedPoints handling
        //TODO Caching von ManeuverDetector attribut in TrackedRaceImpl?
        //TODO Tests
        TrackTimeInfo startAndEndTimePoints = getTrackingStartAndEndTimePoints(competitor);
        if (startAndEndTimePoints != null) {
            TimePoint earliestManeuverStart = startAndEndTimePoints.getTrackStartTimePoint();
            TimePoint latestManeuverEnd = startAndEndTimePoints.getTrackEndTimePoint();
            TimePoint latestRawFixTimePoint = startAndEndTimePoints.getLatestRawFixTimePoint();
            Iterable<GPSFixMoving> douglasPeuckerFixes = trackedRace.approximate(competitor,
                    competitor.getBoat().getBoatClass().getMaximumDistanceForCourseApproximation(),
                    earliestManeuverStart, latestManeuverEnd);
            ManeuverDetectionResult lastManeuverDetectionResult = existingManeuverSpotsPerCompetitor.get(competitor);
            List<ManeuverSpot> maneuverSpots;
            if (lastManeuverDetectionResult == null) {
                maneuverSpots = detectManeuvers(competitor, douglasPeuckerFixes, earliestManeuverStart,
                        latestManeuverEnd);
            } else {
                maneuverSpots = new ArrayList<>();
                long maxDurationForDouglasPeuckerFixExtensionInManeuverAnalysisInMillis = getMaxDurationForDouglasPeuckerFixExtensionInManeuverAnalysis(
                        competitor).asMillis();
                TimePoint latestRawFixTimePointOfPreviousManeuverDetectionIteration = lastManeuverDetectionResult
                        .getLatestRawFixTimePoint();
                ManeuverSpot previouslyDetectedManeuverSpotWithSameDouglasPeuckerPoints = null;
                List<GPSFixMoving> iteratedButNotAnalysedNewDouglasPeuckerFixes = new ArrayList<>();
                boolean skipNextCallOfNewDouglasPeuckerFixesIterator = false;
                GPSFixMoving newDouglasPeuckerFix = null;
                for (Iterator<GPSFixMoving> newDouglasPeuckerFixesIterator = douglasPeuckerFixes
                        .iterator(); newDouglasPeuckerFixesIterator.hasNext();) {
                    if (skipNextCallOfNewDouglasPeuckerFixesIterator) {
                        skipNextCallOfNewDouglasPeuckerFixesIterator = false;
                    } else {
                        newDouglasPeuckerFix = newDouglasPeuckerFixesIterator.next();
                    }
                    ManeuverSpot maneuverSpot = getExistingManeuverSpotByFirstDouglasPeuckerFix(newDouglasPeuckerFix);
                    if (maneuverSpot != null) {
                        boolean douglasPeuckerFixesGroupIsNearlySame = true;
                        // In the previous iteration we have detected a maneuver spot with nearly same douglas peucker
                        // group as maneuver skeleton. In this run, an instantly following maneuver spot for the next
                        // iterated douglas peucker fix has been detected. That means that the previously detected
                        // maneuver spot with its maneuvers may be completely reused.
                        if (previouslyDetectedManeuverSpotWithSameDouglasPeuckerPoints != null) {
                            // Exceptionally, if the maneuver
                            // spot is lying within time range of latestRawFix.getTimePoint() - (longest maneuver
                            // duration)
                            // and
                            // latestRawFixTimePoint.after(latestRawFixTimePointOfPreviousManeuverDetectionIteration),
                            // then we need to recalculate the maneuver spot, because the boundaries of maneuver may get
                            // extended by new incoming fixes
                            boolean maneuverSpotIsFarEnoughFromLatestRawFix = isManeuverSpotFarEnoughFromLatestRawFix(
                                    latestRawFixTimePoint,
                                    maxDurationForDouglasPeuckerFixExtensionInManeuverAnalysisInMillis,
                                    latestRawFixTimePointOfPreviousManeuverDetectionIteration,
                                    previouslyDetectedManeuverSpotWithSameDouglasPeuckerPoints);

                            if (maneuverSpotIsFarEnoughFromLatestRawFix) {
                                maneuverSpots.add(previouslyDetectedManeuverSpotWithSameDouglasPeuckerPoints);
                            } else {
                                for (GPSFixMoving fix : previouslyDetectedManeuverSpotWithSameDouglasPeuckerPoints
                                        .getDouglasPeuckerFixes()) {
                                    iteratedButNotAnalysedNewDouglasPeuckerFixes.add(fix);
                                }
                            }
                            previouslyDetectedManeuverSpotWithSameDouglasPeuckerPoints = null;
                        }
                        if (!iteratedButNotAnalysedNewDouglasPeuckerFixes.isEmpty()) {
//                            getCourseChangeAroundFix(competitor, earliestCourseChangeAnalysisStart, fix, latestCourseChangeAnalysisEnd)
                            if (checkWhetherDouglasPeuckerFixesCanBeGroupedTogether(competitor,
                                    iteratedButNotAnalysedNewDouglasPeuckerFixes
                                            .get(iteratedButNotAnalysedNewDouglasPeuckerFixes.size() - 1),
                                    newDouglasPeuckerFix)) {
                                douglasPeuckerFixesGroupIsNearlySame = false;
                            } else {
                                maneuverSpots.addAll(
                                        detectManeuvers(competitor, iteratedButNotAnalysedNewDouglasPeuckerFixes,
                                                earliestManeuverStart, latestManeuverEnd));
                                iteratedButNotAnalysedNewDouglasPeuckerFixes.clear();
                            }
                        }

                        if (douglasPeuckerFixesGroupIsNearlySame) {
                            boolean firstLoop = true;
                            for (Iterator<GPSFixMoving> existingDouglasPeuckerFixesGroupIterator = maneuverSpot
                                    .getDouglasPeuckerFixes().iterator(); existingDouglasPeuckerFixesGroupIterator
                                            .hasNext();) {
                                if (firstLoop) {
                                    // no check needed, as the first douglas peucker point has been already identified
                                    // as nearly similar with
                                    // getExistingManeuverSpotByFirstDouglasPeuckerFix(newDouglasPeuckerFix)
                                    firstLoop = false;
                                } else {
                                    GPSFixMoving existingDouglasPeuckerFix = existingDouglasPeuckerFixesGroupIterator
                                            .next();
                                    if (!checkDouglasPeuckerFixesForBeingNearlySame(existingDouglasPeuckerFix,
                                            newDouglasPeuckerFix)) {
                                        douglasPeuckerFixesGroupIsNearlySame = false;
                                        skipNextCallOfNewDouglasPeuckerFixesIterator = true;
                                        break;
                                    }
                                }
                                if (existingDouglasPeuckerFixesGroupIterator.hasNext()) {
                                    if (newDouglasPeuckerFixesIterator.hasNext()) {
                                        newDouglasPeuckerFix = newDouglasPeuckerFixesIterator.next();
                                        iteratedButNotAnalysedNewDouglasPeuckerFixes.add(newDouglasPeuckerFix);
                                    } else {
                                        douglasPeuckerFixesGroupIsNearlySame = false;
                                        break;
                                    }
                                }
                            }
                            if (douglasPeuckerFixesGroupIsNearlySame
                                    && !checkManeuverSpotWindsNearlySame(maneuverSpot)) {
                                douglasPeuckerFixesGroupIsNearlySame = false;
                            }
                            if (douglasPeuckerFixesGroupIsNearlySame) {
                                previouslyDetectedManeuverSpotWithSameDouglasPeuckerPoints = maneuverSpot;
                                iteratedButNotAnalysedNewDouglasPeuckerFixes.clear();
                            }
                        }
                    } else if (previouslyDetectedManeuverSpotWithSameDouglasPeuckerPoints != null) {
                        iteratedButNotAnalysedNewDouglasPeuckerFixes.clear();
                        for (GPSFixMoving fix : previouslyDetectedManeuverSpotWithSameDouglasPeuckerPoints
                                .getDouglasPeuckerFixes()) {
                            iteratedButNotAnalysedNewDouglasPeuckerFixes.add(fix);
                        }
                        iteratedButNotAnalysedNewDouglasPeuckerFixes.add(newDouglasPeuckerFix);
                    }
                }
                if (previouslyDetectedManeuverSpotWithSameDouglasPeuckerPoints != null) {
                    boolean maneuverSpotIsFarEnoughFromLatestRawFix = isManeuverSpotFarEnoughFromLatestRawFix(
                            latestRawFixTimePoint,
                            maxDurationForDouglasPeuckerFixExtensionInManeuverAnalysisInMillis,
                            latestRawFixTimePointOfPreviousManeuverDetectionIteration,
                            previouslyDetectedManeuverSpotWithSameDouglasPeuckerPoints);

                    if (maneuverSpotIsFarEnoughFromLatestRawFix) {
                        maneuverSpots.add(previouslyDetectedManeuverSpotWithSameDouglasPeuckerPoints);
                    } else {
                        for (GPSFixMoving fix : previouslyDetectedManeuverSpotWithSameDouglasPeuckerPoints
                                .getDouglasPeuckerFixes()) {
                            iteratedButNotAnalysedNewDouglasPeuckerFixes.add(fix);
                        }
                    }
                }
                if (!iteratedButNotAnalysedNewDouglasPeuckerFixes.isEmpty()) {
                    maneuverSpots.addAll(detectManeuvers(competitor, iteratedButNotAnalysedNewDouglasPeuckerFixes,
                            earliestManeuverStart, latestManeuverEnd));
                }
            }
            existingManeuverSpotsPerCompetitor.put(competitor,
                    new ManeuverDetectionResult(latestRawFixTimePoint, maneuverSpots));
            return getAllManeuversFromManeuverSpots(maneuverSpots);
        }
        throw new NoFixesException();
    }

    private boolean checkWhetherDouglasPeuckerFixesCanBeGroupedTogether(Competitor competitor,
            GPSFixMoving gpsFixMoving, GPSFixMoving newDouglasPeuckerFix) {
        // TODO remove method and replace with impl from ManeuverDetectorImpl
        return false;
    }

    private boolean isManeuverSpotFarEnoughFromLatestRawFix(TimePoint latestRawFixTimePoint,
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

    private boolean checkManeuverSpotWindsNearlySame(ManeuverSpot maneuverSpot) {
        // TODO Auto-generated method stub
        return false;
    }

    private boolean checkDouglasPeuckerFixesForBeingNearlySame(GPSFixMoving existingDouglasPeuckerFix,
            GPSFixMoving newDouglasPeuckerFix) {
        // TODO Auto-generated method stub
        return false;
    }

    private ManeuverSpot getExistingManeuverSpotByFirstDouglasPeuckerFix(GPSFixMoving newDouglasPeuckerFix) {
        // TODO Auto-generated method stub
        return null;
    }

}

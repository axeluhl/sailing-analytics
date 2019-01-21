package com.sap.sailing.windestimation.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SpeedWithBearingWithConfidence;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.maneuverdetection.impl.ManeuverMainCurveDetailsWithBearingSteps;
import com.sap.sailing.domain.maneuverdetection.impl.TrackTimeInfo;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.CompleteManeuverCurve;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.ManeuverCurveBoundaries;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.windestimation.aggregator.msthmm.MstGraphNodeTransitionProbabilitiesCalculator;
import com.sap.sailing.windestimation.aggregator.msthmm.MstManeuverGraphGenerator;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.transformer.ManeuverForEstimationTransformer;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverClassifiersCache;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverWithProbabilisticTypeClassification;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsDurationImpl;

public class IncrementalMstManeuverGraphGenerator extends MstManeuverGraphGenerator {

    private final Map<Competitor, ManeuverDataOfCompetitor> maneuverDataPerCompetitor = new HashMap<>();
    private final ManeuverClassifiersCache maneuverClassifiersCache;
    private final TrackedRace trackedRace;
    private int numberOfNonTemporaryManeuvers;
    private final ManeuverForEstimationTransformer maneuverForEstimationTransformer = new ManeuverForEstimationTransformer();
    private final PolarDataService polarService;

    public IncrementalMstManeuverGraphGenerator(TrackedRace trackedRace,
            MstGraphNodeTransitionProbabilitiesCalculator transitionProbabilitiesCalculator,
            ManeuverClassifiersCache maneuverClassifiersCache, PolarDataService polarService) {
        super(transitionProbabilitiesCalculator);
        this.trackedRace = trackedRace;
        this.maneuverClassifiersCache = maneuverClassifiersCache;
        this.polarService = polarService;
    }

    public void add(Competitor competitor, CompleteManeuverCurve newManeuver, TrackTimeInfo trackTimeInfo) {
        ManeuverDataOfCompetitor maneuverData = maneuverDataPerCompetitor.get(competitor);
        if (maneuverData == null) {
            maneuverData = new ManeuverDataOfCompetitor();
            maneuverDataPerCompetitor.put(competitor, maneuverData);
        }
        CompleteManeuverCurve latestNonTemporaryAcceptedManeuver = maneuverData.getLatestNonTemporaryAcceptedManeuver();
        // ignore maneuvers older than the last non-temporary maneuver
        if (latestNonTemporaryAcceptedManeuver == null
                || newManeuver.getTimePoint().after(latestNonTemporaryAcceptedManeuver.getTimePoint())) {
            CompleteManeuverCurve latestTemporaryAcceptedManeuver = maneuverData.getLatestTemporaryAcceptedManeuver();
            CompleteManeuverCurve previousManeuver;
            CompleteManeuverCurve nextManeuver;
            if (latestTemporaryAcceptedManeuver != null
                    && isLocationOfManeuversNearlySame(latestTemporaryAcceptedManeuver, newManeuver)) {
                // replace temporary accepted maneuver
                previousManeuver = maneuverData.getPreviousManeuver(latestTemporaryAcceptedManeuver);
                nextManeuver = null;
            } else if (latestTemporaryAcceptedManeuver != null) {
                // ignore maneuvers before latest temporary accepted maneuver
                if (latestTemporaryAcceptedManeuver.getTimePoint().before(newManeuver.getTimePoint())) {
                    previousManeuver = latestTemporaryAcceptedManeuver;
                    nextManeuver = null;
                    // check if latestTemporaryAcceptedManeuver is cleaning considering the newManeuver as next maneuver
                    ManeuverForEstimation temporaryAcceptedManeuverForEstimation = convertCleanManeuverSpotToManeuverForEstimation(
                            latestTemporaryAcceptedManeuver,
                            maneuverData.getPreviousManeuver(latestTemporaryAcceptedManeuver), newManeuver, competitor,
                            trackTimeInfo);
                    if (temporaryAcceptedManeuverForEstimation.isClean()) {
                        maneuverData.getNonTemporaryAcceptedManeuverClassificationsToAdd()
                                .add(maneuverData.getLatestTemporaryAcceptedManeuverClassification());
                        maneuverData.setLatestNonTemporaryAcceptedManeuver(latestTemporaryAcceptedManeuver);
                        for (Iterator<CompleteManeuverCurve> iterator = maneuverData
                                .getAllManeuversAfterLatestNonTemporaryAcceptedManeuver().iterator(); iterator
                                        .hasNext();) {
                            CompleteManeuverCurve maneuver = iterator.next();
                            if (!maneuver.getTimePoint().after(latestTemporaryAcceptedManeuver.getTimePoint())) {
                                iterator.remove();
                            }
                        }
                    }
                } else {
                    return;
                }
            } else {
                previousManeuver = maneuverData.getPreviousManeuver(newManeuver);
                nextManeuver = null;
            }
            ManeuverForEstimation newManeuverForEstimation = convertCleanManeuverSpotToManeuverForEstimation(
                    newManeuver, previousManeuver, nextManeuver, competitor, trackTimeInfo);
            if (!newManeuverForEstimation.isClean()) {
                maneuverData.setLatestTemporaryAcceptedManeuver(null);
                maneuverData.setLatestTemporaryAcceptedManeuverClassification(null);
            } else {
                maneuverData.setLatestTemporaryAcceptedManeuver(newManeuver);
                ManeuverWithProbabilisticTypeClassification newManeuverClassification = maneuverClassifiersCache
                        .classifyInstance(newManeuverForEstimation);
                maneuverData.setLatestTemporaryAcceptedManeuver(newManeuver);
                maneuverData.setLatestTemporaryAcceptedManeuverClassification(newManeuverClassification);
            }
            maneuverData.getAllManeuversAfterLatestNonTemporaryAcceptedManeuver().add(newManeuver);
        }
    }

    private boolean isLocationOfManeuversNearlySame(CompleteManeuverCurve maneuver1, CompleteManeuverCurve maneuver2) {
        if (maneuver1.getTimePoint().equals(maneuver2.getTimePoint())) {
            return true;
        }
        ManeuverMainCurveDetailsWithBearingSteps turningSection1 = maneuver1.getMainCurveBoundaries();
        ManeuverMainCurveDetailsWithBearingSteps turningSection2 = maneuver2.getMainCurveBoundaries();
        TimePoint latestBefore = turningSection1.getTimePointBefore().after(turningSection2.getTimePointBefore())
                ? turningSection1.getTimePointBefore()
                : turningSection2.getTimePointBefore();
        TimePoint earliestAfter = turningSection1.getTimePointAfter().before(turningSection1.getTimePointAfter())
                ? turningSection1.getTimePointAfter()
                : turningSection2.getTimePointAfter();
        return latestBefore.before(earliestAfter);
    }

    private ManeuverForEstimation convertCleanManeuverSpotToManeuverForEstimation(CompleteManeuverCurve maneuver,
            CompleteManeuverCurve previousManeuver, CompleteManeuverCurve nextManeuver, Competitor competitor,
            TrackTimeInfo trackTimeInfo) {
        BoatClass boatClass = trackedRace.getBoatOfCompetitor(competitor).getBoatClass();
        ManeuverCurveBoundaries maneuverCurveBoundaries = maneuver.getManeuverCurveWithStableSpeedAndCourseBoundaries();
        GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
        Position maneuverPosition = track.getEstimatedPosition(maneuver.getTimePoint(), false);
        Double courseChangeInDegreesWithinTurningSectionOfPreviousManeuver = previousManeuver == null ? null
                : previousManeuver.getMainCurveBoundaries().getDirectionChangeInDegrees();
        Double courseChangeInDegreesWithinTurningSectionOfNextManeuver = nextManeuver.getMainCurveBoundaries()
                .getDirectionChangeInDegrees();
        Double targetTackAngleInDegrees = getTargetTackAngleInDegrees(maneuverCurveBoundaries, boatClass);
        Double targetJibeAngleInDegrees = getTargetJibeAngleInDegrees(maneuverCurveBoundaries, boatClass);
        boolean markPassingDataAvailable = maneuver.isMarkPassing()
                || hasNextWaypoint(competitor, maneuverCurveBoundaries.getTimePointAfter());
        Duration durationFromPreviousManeuverEndToManeuverStart = previousManeuver == null
                ? trackTimeInfo.getTrackStartTimePoint().until(maneuverCurveBoundaries.getTimePointBefore())
                : previousManeuver.getManeuverCurveWithStableSpeedAndCourseBoundaries().getTimePointAfter()
                        .until(maneuverCurveBoundaries.getTimePointBefore());
        Duration durationFromManeuverEndToNextManeuverStart = maneuverCurveBoundaries.getTimePointAfter()
                .until(nextManeuver == null ? trackTimeInfo.getTrackEndTimePoint()
                        : nextManeuver.getManeuverCurveWithStableSpeedAndCourseBoundaries().getTimePointBefore());
        Duration longestIntervalBetweenTwoFixes = null;
        Duration intervalBetweenFirstFixOfCurveAndPreviousFix = Duration.ONE_YEAR;
        GPSFixMoving lastFixBeforeManeuver = track.getLastFixBefore(maneuverCurveBoundaries.getTimePointBefore());
        if (lastFixBeforeManeuver != null) {
            intervalBetweenFirstFixOfCurveAndPreviousFix = lastFixBeforeManeuver.getTimePoint()
                    .until(maneuverCurveBoundaries.getTimePointBefore());
        }
        Duration intervalBetweenLastFixOfCurveAndNextFix = Duration.ONE_YEAR;
        GPSFixMoving firstFixAfterManeuver = track.getFirstFixAfter(maneuverCurveBoundaries.getTimePointAfter());
        if (firstFixAfterManeuver != null) {
            intervalBetweenLastFixOfCurveAndNextFix = maneuverCurveBoundaries.getTimePointAfter()
                    .until(firstFixAfterManeuver.getTimePoint());
        }
        GPSFixMoving previousFix = null;
        long longestIntervalBetweenTwoFixesInMillis = 0;
        track.lockForRead();
        try {
            for (GPSFixMoving fix : track.getFixes(maneuverCurveBoundaries.getTimePointBefore(), true,
                    maneuverCurveBoundaries.getTimePointAfter(), true)) {
                if (previousFix != null) {
                    long intervalBetweenPreviousAndCurrentFixInMillis = previousFix.getTimePoint()
                            .until(fix.getTimePoint()).asMillis();
                    if (longestIntervalBetweenTwoFixesInMillis < intervalBetweenPreviousAndCurrentFixInMillis) {
                        longestIntervalBetweenTwoFixesInMillis = intervalBetweenPreviousAndCurrentFixInMillis;
                    }
                }
            }
        } finally {
            track.unlockAfterRead();
        }
        longestIntervalBetweenTwoFixes = new MillisecondsDurationImpl(longestIntervalBetweenTwoFixesInMillis);
        ConvertableManeuverForEstimationAdapterForCompleteManeuverCurve convertableManeuver = new ConvertableManeuverForEstimationAdapterForCompleteManeuverCurve(
                maneuver, maneuverPosition, markPassingDataAvailable, longestIntervalBetweenTwoFixes,
                courseChangeInDegreesWithinTurningSectionOfPreviousManeuver,
                courseChangeInDegreesWithinTurningSectionOfNextManeuver, intervalBetweenFirstFixOfCurveAndPreviousFix,
                intervalBetweenLastFixOfCurveAndNextFix, durationFromPreviousManeuverEndToManeuverStart,
                durationFromManeuverEndToNextManeuverStart, targetTackAngleInDegrees, targetJibeAngleInDegrees);
        // TODO compute scaledSpeedDivisor, recompute maneuverForEstimation, reclassify all maneuver instances if
        // scaledSpeedDivisor has significantly changed?
        ManeuverForEstimation maneuverForEstimation = maneuverForEstimationTransformer
                .getManeuverForEstimation(convertableManeuver, 1.0, boatClass);
        return maneuverForEstimation;
    }

    private Double getTargetTackAngleInDegrees(ManeuverCurveBoundaries curveWithUnstableCourseAndSpeed,
            BoatClass boatClass) {
        Double targetTackAngle = null;
        Speed boatSpeed = curveWithUnstableCourseAndSpeed.getSpeedWithBearingBefore()
                .compareTo(curveWithUnstableCourseAndSpeed.getSpeedWithBearingAfter()) < 0
                        ? curveWithUnstableCourseAndSpeed.getSpeedWithBearingBefore()
                        : curveWithUnstableCourseAndSpeed.getSpeedWithBearingAfter();
        if (polarService.getAllBoatClassesWithPolarSheetsAvailable().contains(boatClass)) {
            SpeedWithBearingWithConfidence<Void> closestTackTwa = polarService.getClosestTwaTws(ManeuverType.TACK,
                    boatSpeed, curveWithUnstableCourseAndSpeed.getDirectionChangeInDegrees(), boatClass);
            if (closestTackTwa != null) {
                targetTackAngle = polarService.getManeuverAngleInDegreesFromTwa(ManeuverType.TACK,
                        closestTackTwa.getObject().getBearing());
            }
        }
        return targetTackAngle;
    }

    private boolean hasNextWaypoint(Competitor competitor, TimePoint timePoint) {
        TrackedLegOfCompetitor legAfter = trackedRace.getTrackedLeg(competitor, timePoint);
        if (legAfter != null && legAfter.getLeg().getTo() != null) {
            return true;
        }
        return false;
    }

    private Double getTargetJibeAngleInDegrees(ManeuverCurveBoundaries curveWithUnstableCourseAndSpeed,
            BoatClass boatClass) {
        Double targetJibeAngle = null;
        Speed boatSpeed = curveWithUnstableCourseAndSpeed.getSpeedWithBearingBefore()
                .compareTo(curveWithUnstableCourseAndSpeed.getSpeedWithBearingAfter()) < 0
                        ? curveWithUnstableCourseAndSpeed.getSpeedWithBearingBefore()
                        : curveWithUnstableCourseAndSpeed.getSpeedWithBearingAfter();
        if (polarService.getAllBoatClassesWithPolarSheetsAvailable().contains(boatClass)) {
            SpeedWithBearingWithConfidence<Void> closestJibeTwa = polarService.getClosestTwaTws(ManeuverType.JIBE,
                    boatSpeed, curveWithUnstableCourseAndSpeed.getDirectionChangeInDegrees(), boatClass);
            if (closestJibeTwa != null) {
                targetJibeAngle = polarService.getManeuverAngleInDegreesFromTwa(ManeuverType.JIBE,
                        closestJibeTwa.getObject().getBearing());
            }
        }
        return targetJibeAngle;
    }

    @Override
    public MstManeuverGraphComponents parseGraph() {
        List<ManeuverWithProbabilisticTypeClassification> nonTemporaryNodes = new ArrayList<>(
                numberOfNonTemporaryManeuvers);
        List<ManeuverWithProbabilisticTypeClassification> temporaryNodes = new ArrayList<>();
        for (ManeuverDataOfCompetitor maneuverData : maneuverDataPerCompetitor.values()) {
            nonTemporaryNodes.addAll(maneuverData.getNonTemporaryAcceptedManeuverClassificationsToAdd());
            temporaryNodes.add(maneuverData.getLatestTemporaryAcceptedManeuverClassification());
            maneuverData.getNonTemporaryAcceptedManeuverClassificationsToAdd().clear();
        }
        Collections.sort(nonTemporaryNodes);
        for (ManeuverWithProbabilisticTypeClassification node : nonTemporaryNodes) {
            addNode(node);
        }
        if (temporaryNodes.isEmpty()) {
            return super.parseGraph();
        }
        MstManeuverGraphGenerator clonedMstGenerator = clone();
        Collections.sort(temporaryNodes);
        for (ManeuverWithProbabilisticTypeClassification node : temporaryNodes) {
            clonedMstGenerator.addNode(node);
        }
        return clonedMstGenerator.parseGraph();
    }

    private static class ManeuverDataOfCompetitor {
        private CompleteManeuverCurve latestNonTemporaryAcceptedManeuver = null;
        private CompleteManeuverCurve latestTemporaryAcceptedManeuver = null;
        private ManeuverWithProbabilisticTypeClassification latestTemporaryAcceptedManeuverClassification = null;
        private final List<ManeuverWithProbabilisticTypeClassification> nonTemporaryAcceptedManeuverClassificationsToAdd = new ArrayList<>();
        private final SortedSet<CompleteManeuverCurve> allManeuversAfterLatestNonTemporaryAcceptedManeuver = new TreeSet<>(
                new Comparator<CompleteManeuverCurve>() {
                    @Override
                    public int compare(CompleteManeuverCurve o1, CompleteManeuverCurve o2) {
                        return o1.getTimePoint().compareTo(o2.getTimePoint());
                    }
                });

        public CompleteManeuverCurve getLatestNonTemporaryAcceptedManeuver() {
            return latestNonTemporaryAcceptedManeuver;
        }

        public void setLatestNonTemporaryAcceptedManeuver(CompleteManeuverCurve latestNonTemporaryAcceptedManeuver) {
            this.latestNonTemporaryAcceptedManeuver = latestNonTemporaryAcceptedManeuver;
        }

        public CompleteManeuverCurve getLatestTemporaryAcceptedManeuver() {
            return latestTemporaryAcceptedManeuver;
        }

        public void setLatestTemporaryAcceptedManeuver(CompleteManeuverCurve latestTemporaryAcceptedManeuver) {
            this.latestTemporaryAcceptedManeuver = latestTemporaryAcceptedManeuver;
        }

        public ManeuverWithProbabilisticTypeClassification getLatestTemporaryAcceptedManeuverClassification() {
            return latestTemporaryAcceptedManeuverClassification;
        }

        public void setLatestTemporaryAcceptedManeuverClassification(
                ManeuverWithProbabilisticTypeClassification latestTemporaryAcceptedManeuverClassification) {
            this.latestTemporaryAcceptedManeuverClassification = latestTemporaryAcceptedManeuverClassification;
        }

        public List<ManeuverWithProbabilisticTypeClassification> getNonTemporaryAcceptedManeuverClassificationsToAdd() {
            return nonTemporaryAcceptedManeuverClassificationsToAdd;
        }

        public SortedSet<CompleteManeuverCurve> getAllManeuversAfterLatestNonTemporaryAcceptedManeuver() {
            return allManeuversAfterLatestNonTemporaryAcceptedManeuver;
        }

        public CompleteManeuverCurve getPreviousManeuver(CompleteManeuverCurve maneuver) {
            CompleteManeuverCurve previousManeuver = getLatestNonTemporaryAcceptedManeuver();
            for (CompleteManeuverCurve nextManeuver : getAllManeuversAfterLatestNonTemporaryAcceptedManeuver()) {
                if (!nextManeuver.getTimePoint().before(maneuver.getTimePoint())) {
                    return previousManeuver;
                }
                previousManeuver = nextManeuver;
            }
            return previousManeuver;
        }
    }

}

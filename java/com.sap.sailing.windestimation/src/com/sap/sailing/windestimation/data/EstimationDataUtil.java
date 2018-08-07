package com.sap.sailing.windestimation.data;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.maneuverdetection.GpsFixWithEstimationData;
import com.sap.sailing.domain.maneuverdetection.impl.GpsFixWithEstimationDataImpl;
import com.sap.sailing.domain.maneuverdetection.impl.ManeuverDetectorImpl;
import com.sap.sailing.domain.maneuverdetection.impl.ManeuverDetectorWithEstimationDataSupportDecoratorImpl;
import com.sap.sailing.domain.maneuverdetection.impl.TrackTimeInfo;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.CompleteManeuverCurve;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Util;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class EstimationDataUtil {

    private EstimationDataUtil() {
    }

    // TODO replace GpsFixesWithEstimationDataSerializer with serializers using this methods
    public static List<CompetitorTrackWithEstimationData<GpsFixWithEstimationData>> getCompetitorTracksWithGPSFixWithEstimationData(
            TrackedRace trackedRace, PolarDataService polarDataService, boolean smoothFixes) {
        List<CompetitorTrackWithEstimationData<GpsFixWithEstimationData>> competitorTracks = new ArrayList<>();
        for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
            ManeuverDetectorImpl maneuverDetector = new ManeuverDetectorImpl(trackedRace, competitor);
            TrackTimeInfo trackTimeInfo = maneuverDetector.getTrackTimeInfo();
            if (trackTimeInfo != null) {
                ManeuverDetectorWithEstimationDataSupportDecoratorImpl estimationDataSupportDecoratorImpl = new ManeuverDetectorWithEstimationDataSupportDecoratorImpl(
                        maneuverDetector, polarDataService);
                GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
                List<GpsFixWithEstimationData> gpsFixesWithEstimationData = new ArrayList<>();
                track.lockForRead();
                try {
                    for (GPSFixMoving gpsFix : track.getFixes(trackTimeInfo.getTrackStartTimePoint(), true,
                            trackTimeInfo.getTrackEndTimePoint(), true)) {
                        Wind wind = trackedRace.getWind(gpsFix.getPosition(), gpsFix.getTimePoint());
                        Distance closestDistanceToMark = estimationDataSupportDecoratorImpl
                                .getClosestDistanceToMark(gpsFix.getTimePoint());
                        SpeedWithBearing speedWithBearing = smoothFixes ? track.getEstimatedSpeed(gpsFix.getTimePoint())
                                : gpsFix.getSpeed();
                        Bearing relativeBearingToNextMark = speedWithBearing == null ? null
                                : estimationDataSupportDecoratorImpl.getRelativeBearingToNextMark(gpsFix.getTimePoint(),
                                        speedWithBearing.getBearing());
                        GpsFixWithEstimationData gpsFixWithEstimationData = new GpsFixWithEstimationDataImpl(
                                gpsFix.getPosition(), gpsFix.getTimePoint(), speedWithBearing, wind,
                                relativeBearingToNextMark, closestDistanceToMark);
                        gpsFixesWithEstimationData.add(gpsFixWithEstimationData);
                    }
                } finally {
                    track.unlockAfterRead();
                }
                CompetitorTrackWithEstimationData<GpsFixWithEstimationData> competitorTrack = createCompetitorTrack(
                        trackedRace, polarDataService, competitor, trackTimeInfo, gpsFixesWithEstimationData);
                competitorTracks.add(competitorTrack);
            }
        }
        return competitorTracks;
    }

    public static List<CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData>> getCompetitorTracksWithManeuverEstimationData(
            TrackedRace trackedRace, PolarDataService polarDataService) {
        // TODO make iterative
        Iterable<Competitor> competitors = trackedRace.getRace().getCompetitors();
        ManeuverDetectorWithEstimationDataSupportDecoratorImpl maneuverDetector = new ManeuverDetectorWithEstimationDataSupportDecoratorImpl(
                new ManeuverDetectorImpl(), polarDataService);
        List<CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData>> competitorTracks = new ArrayList<>();
        for (Competitor competitor : competitors) {
            TrackTimeInfo trackTimeInfo = maneuverDetector.getTrackTimeInfo();
            if (trackTimeInfo != null) {
                Iterable<Maneuver> maneuvers = trackedRace.getManeuvers(competitor, false);
                List<CompleteManeuverCurve> completeManeuverCurves = maneuverDetector
                        .getCompleteManeuverCurves(maneuvers);
                List<CompleteManeuverCurveWithEstimationData> completeManeuverCurvesWithEstimationData = maneuverDetector
                        .getCompleteManeuverCurvesWithEstimationData(completeManeuverCurves);
                CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData> competitorTrackWithEstimationData = createCompetitorTrack(
                        trackedRace, polarDataService, competitor, trackTimeInfo,
                        completeManeuverCurvesWithEstimationData);
                competitorTracks.add(competitorTrackWithEstimationData);
            }

        }
        return competitorTracks;
    }

    public static List<CompetitorTrackWithEstimationData<ManeuverForClassification>> getCompetitorTracksWithManeuversForClassification(
            List<CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData>> competitorTracksWithManeuverEstimationData) {
        List<CompetitorTrackWithEstimationData<ManeuverForClassification>> competitorTracks = new ArrayList<>();
        for (CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData> otherCompetitorTrack : competitorTracksWithManeuverEstimationData) {
            List<ManeuverForClassification> maneuversForClassification = new ArrayList<>();
            CompleteManeuverCurveWithEstimationData previousManeuver = null;
            CompleteManeuverCurveWithEstimationData currentManeuver = null;
            for (CompleteManeuverCurveWithEstimationData nextManeuver : otherCompetitorTrack.getElements()) {
                if (currentManeuver != null) {
                    ManeuverForClassification maneuverForClassification = getManeuverForClassification(currentManeuver,
                            previousManeuver, nextManeuver);
                    maneuversForClassification.add(maneuverForClassification);
                }
                previousManeuver = currentManeuver;
                currentManeuver = nextManeuver;
            }
            if (currentManeuver != null) {
                ManeuverForClassification maneuverForClassification = getManeuverForClassification(currentManeuver,
                        previousManeuver, null);
                maneuversForClassification.add(maneuverForClassification);
            }
            CompetitorTrackWithEstimationData<ManeuverForClassification> competitorTrack = new CompetitorTrackWithEstimationData<>(
                    otherCompetitorTrack.getCompetitorName(), otherCompetitorTrack.getBoatClass(),
                    maneuversForClassification, otherCompetitorTrack.getAvgIntervalBetweenFixesInSeconds(),
                    otherCompetitorTrack.getDistanceTravelled(), otherCompetitorTrack.getTrackStartTimePoint(),
                    otherCompetitorTrack.getTrackEndTimePoint(), otherCompetitorTrack.getFixesCountForPolars(),
                    otherCompetitorTrack.getMarkPassingsCount(), otherCompetitorTrack.getWaypointsCount());
            competitorTracks.add(competitorTrack);
        }
        return competitorTracks;
    }

    public static ManeuverForClassification getManeuverForClassification(
            CompleteManeuverCurveWithEstimationData maneuver, CompleteManeuverCurveWithEstimationData previousManeuver,
            CompleteManeuverCurveWithEstimationData nextManeuver) {
        ManeuverTypeForClassification maneuverType = getManeuverTypeForClassification(maneuver);
        double absoluteTotalCourseChangeInDegrees = Math
                .abs(maneuver.getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees());
        double speedInSpeedOutRatio = maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter()
                .getKnots() < 0.1 ? 0
                        : maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore().getKnots()
                                / maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getKnots();
        double oversteeringInDegrees = Math
                .abs(maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getBearing()
                        .getDifferenceTo(maneuver.getMainCurve().getSpeedWithBearingAfter().getBearing()).getDegrees());
        double speedLossRatio = maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore().getKnots() > 0
                ? maneuver.getCurveWithUnstableCourseAndSpeed().getLowestSpeed().getKnots()
                        / maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore().getKnots()
                : 0;
        double speedGainRatio = maneuver.getMainCurve().getHighestSpeed().getKnots() > 0
                ? maneuver.getMainCurve().getSpeedWithBearingBefore().getKnots()
                        / maneuver.getMainCurve().getHighestSpeed().getKnots()
                : 0;
        Double deviationFromOptimalTackAngleInDegrees = maneuver.getTargetTackAngleInDegrees() == null ? null
                : Math.abs(maneuver.getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees())
                        - maneuver.getTargetTackAngleInDegrees();
        Double deviationFromOptimalJibeAngleInDegrees = maneuver.getTargetJibeAngleInDegrees() == null ? null
                : Math.abs(maneuver.getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees())
                        - maneuver.getTargetJibeAngleInDegrees();
        Double highestAbsoluteDeviationOfBoatsCourseToBearingFromBoatToNextWaypointInDegrees;
        if (maneuver.getRelativeBearingToNextMarkBeforeManeuver() == null
                && maneuver.getRelativeBearingToNextMarkAfterManeuver() == null) {
            highestAbsoluteDeviationOfBoatsCourseToBearingFromBoatToNextWaypointInDegrees = null;
        } else if (maneuver.getRelativeBearingToNextMarkBeforeManeuver() == null) {
            highestAbsoluteDeviationOfBoatsCourseToBearingFromBoatToNextWaypointInDegrees = Math
                    .abs(maneuver.getRelativeBearingToNextMarkAfterManeuver().getDegrees());
        } else if (maneuver.getRelativeBearingToNextMarkAfterManeuver() == null) {
            highestAbsoluteDeviationOfBoatsCourseToBearingFromBoatToNextWaypointInDegrees = Math
                    .abs(maneuver.getRelativeBearingToNextMarkBeforeManeuver().getDegrees());
        } else {
            highestAbsoluteDeviationOfBoatsCourseToBearingFromBoatToNextWaypointInDegrees = Math
                    .abs(maneuver.getRelativeBearingToNextMarkBeforeManeuver().getDegrees()) > Math
                            .abs(maneuver.getRelativeBearingToNextMarkAfterManeuver().getDegrees())
                                    ? Math.abs(maneuver.getRelativeBearingToNextMarkBeforeManeuver().getDegrees())
                                    : Math.abs(maneuver.getRelativeBearingToNextMarkAfterManeuver().getDegrees());
        }
        double mainCurveDurationInSeconds = maneuver.getMainCurve().getDuration().asSeconds();
        double maneuverDurationInSeconds = maneuver.getCurveWithUnstableCourseAndSpeed().getDuration().asSeconds();
        double recoveryPhaseDurationInSeconds = maneuver.getMainCurve().getTimePointAfter()
                .until(maneuver.getCurveWithUnstableCourseAndSpeed().getTimePointAfter()).asSeconds();
        double timeLossInSeconds = maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore()
                .getMetersPerSecond() > 0
                        ? maneuver.getCurveWithUnstableCourseAndSpeed().getDistanceLost().getMeters()
                                / maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore()
                                        .getMetersPerSecond()
                        : 0;
        boolean clean = maneuver.getCurveWithUnstableCourseAndSpeed().getLongestIntervalBetweenTwoFixes()
                .asSeconds() < 4
                && maneuver.getCurveWithUnstableCourseAndSpeed().getIntervalBetweenFirstFixOfCurveAndPreviousFix()
                        .asSeconds() <= 4
                && maneuver.getCurveWithUnstableCourseAndSpeed().getIntervalBetweenLastFixOfCurveAndNextFix()
                        .asSeconds() <= 4
                && maneuver.getCurveWithUnstableCourseAndSpeed()
                        .getGpsFixesCountFromPreviousManeuverEndToManeuverStart()
                        / maneuver.getCurveWithUnstableCourseAndSpeed()
                                .getDurationFromPreviousManeuverEndToManeuverStart().asSeconds() <= 8
                && maneuver.getCurveWithUnstableCourseAndSpeed().getGpsFixesCountFromManeuverEndToNextManeuverStart()
                        / maneuver.getCurveWithUnstableCourseAndSpeed().getDurationFromManeuverEndToNextManeuverStart()
                                .asSeconds() <= 8
                && (maneuver.getCurveWithUnstableCourseAndSpeed().getDurationFromPreviousManeuverEndToManeuverStart()
                        .asSeconds() >= 4
                        || previousManeuver != null
                                && Math.abs(previousManeuver.getCurveWithUnstableCourseAndSpeed()
                                        .getDirectionChangeInDegrees()) < Math.abs(maneuver
                                                .getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees())
                                                * 0.3)
                && (maneuver.getCurveWithUnstableCourseAndSpeed().getDurationFromManeuverEndToNextManeuverStart()
                        .asSeconds() >= 4
                        || nextManeuver != null
                                && Math.abs(nextManeuver.getCurveWithUnstableCourseAndSpeed()
                                        .getDirectionChangeInDegrees()) < Math.abs(maneuver
                                                .getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees())
                                                * 0.3)
                && maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore().getKnots() > 2
                && maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getKnots() > 2
                && Math.abs(maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore().getKnots()
                        - maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getKnots())
                        * 3 < Math.min(
                                maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore().getKnots(),
                                maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getKnots());
        ManeuverCategory maneuverCategory = getManeuverCategory(maneuver);
        ManeuverForClassification maneuverForClassification = new ManeuverForClassificationImpl(maneuverType,
                absoluteTotalCourseChangeInDegrees, speedInSpeedOutRatio, oversteeringInDegrees, speedLossRatio,
                speedGainRatio, maneuver.getMainCurve().getMaxTurningRateInDegreesPerSecond(),
                deviationFromOptimalTackAngleInDegrees, deviationFromOptimalJibeAngleInDegrees,
                highestAbsoluteDeviationOfBoatsCourseToBearingFromBoatToNextWaypointInDegrees,
                mainCurveDurationInSeconds, maneuverDurationInSeconds, recoveryPhaseDurationInSeconds,
                timeLossInSeconds, clean, maneuverCategory);
        return maneuverForClassification;
    }

    private static ManeuverCategory getManeuverCategory(CompleteManeuverCurveWithEstimationData maneuver) {
        double absCourseChangeInDegrees = Math
                .abs(maneuver.getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees());
        if (absCourseChangeInDegrees < 20) {
            return ManeuverCategory.SMALL;
        }
        if (absCourseChangeInDegrees <= 120) {
            return maneuver.isMarkPassing() ? ManeuverCategory.MARK_PASSING : ManeuverCategory.REGULAR;
        }
        if (absCourseChangeInDegrees <= 300) {
            return ManeuverCategory._180;
        }
        return ManeuverCategory._360;
    }

    private static ManeuverTypeForClassification getManeuverTypeForClassification(
            CompleteManeuverCurveWithEstimationData maneuver) {
        ManeuverType maneuverType = maneuver.getManeuverTypeForCompleteManeuverCurve();
        switch (maneuverType) {
        case BEAR_AWAY:
            return ManeuverTypeForClassification.BEAR_AWAY;
        case HEAD_UP:
            return ManeuverTypeForClassification.HEAD_UP;
        case PENALTY_CIRCLE:
            return ManeuverTypeForClassification._360;
        case UNKNOWN:
            return null;
        case JIBE:
            return Math.abs(maneuver.getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees()) > 120
                    ? ManeuverTypeForClassification._180_JIBE : ManeuverTypeForClassification.JIBE;
        case TACK:
            return Math.abs(maneuver.getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees()) > 120
                    ? ManeuverTypeForClassification._180_TACK : ManeuverTypeForClassification.TACK;
        }
        throw new IllegalStateException();
    }

    private static <T> CompetitorTrackWithEstimationData<T> createCompetitorTrack(TrackedRace trackedRace,
            PolarDataService polarDataService, Competitor competitor, TrackTimeInfo trackTimeInfo,
            List<T> completeManeuverCurvesWithEstimationData) {
        BoatClass boatClass = trackedRace.getBoatOfCompetitor(competitor).getBoatClass();
        GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
        Distance distanceTravelled = track.getDistanceTraveled(trackTimeInfo.getTrackStartTimePoint(),
                trackTimeInfo.getTrackEndTimePoint());
        double avgIntervalBetweenFixesInSeconds = track.getAverageIntervalBetweenFixes().asSeconds();
        CompetitorTrackWithEstimationData<T> competitorTrackWithEstimationData = new CompetitorTrackWithEstimationData<>(
                competitor.getName(), boatClass, completeManeuverCurvesWithEstimationData,
                avgIntervalBetweenFixesInSeconds, distanceTravelled, trackTimeInfo.getTrackStartTimePoint(),
                trackTimeInfo.getTrackEndTimePoint(), getFixesCountForPolars(polarDataService, trackedRace, competitor),
                getMarkPassingsCount(trackedRace, competitor), getWaypointsCount(trackedRace));
        return competitorTrackWithEstimationData;
    }

    // FIXME refactor code duplication with CompetitorTrackWithEstimationDataJsonSerializer
    private static int getMarkPassingsCount(TrackedRace trackedRace, Competitor competitor) {
        int markPassingsCount = 0;
        NavigableSet<MarkPassing> markPassings = trackedRace.getMarkPassings(competitor, false);
        trackedRace.lockForRead(markPassings);
        try {
            markPassingsCount = Util.size(markPassings);
        } finally {
            trackedRace.unlockAfterRead(markPassings);
        }
        return markPassingsCount;
    }

    private static long getFixesCountForPolars(PolarDataService polarDataService, TrackedRace trackedRace,
            Competitor competitor) {
        BoatClass boatClass = trackedRace.getRace().getBoatOfCompetitor(competitor).getBoatClass();
        Long fixesCountForBoatPolars = polarDataService.getFixCountPerBoatClass().get(boatClass);
        return fixesCountForBoatPolars == null ? 0L : fixesCountForBoatPolars;
    }

    private static int getWaypointsCount(TrackedRace trackedRace) {
        Iterable<Waypoint> waypoints = trackedRace.getRace().getCourse().getWaypoints();
        return Util.size(waypoints);
    }

}

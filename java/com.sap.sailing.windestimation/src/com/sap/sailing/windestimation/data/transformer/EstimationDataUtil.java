package com.sap.sailing.windestimation.data.transformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
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
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
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

    public static List<ManeuverForEstimation> getUsefulManeuversSortedByTimePoint(
            List<CompetitorTrackWithEstimationData<ManeuverForEstimation>> competitorTracks) {
        List<ManeuverForEstimation> usefulManeuversSortedByTimePoint = new ArrayList<>();
        for (CompetitorTrackWithEstimationData<ManeuverForEstimation> competitorTrack : competitorTracks) {
            for (ManeuverForEstimation maneuver : competitorTrack.getElements()) {
                if (maneuver.isClean() || maneuver.isCleanBefore() || maneuver.isCleanAfter()) {
                    usefulManeuversSortedByTimePoint.add(maneuver);
                }
            }
        }
        Collections.sort(usefulManeuversSortedByTimePoint,
                (o1, o2) -> o1.getManeuverTimePoint().compareTo(o2.getManeuverTimePoint()));
        return usefulManeuversSortedByTimePoint;
    }

}

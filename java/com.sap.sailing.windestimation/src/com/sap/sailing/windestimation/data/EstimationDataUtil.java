package com.sap.sailing.windestimation.data;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.maneuverdetection.impl.ManeuverDetectorImpl;
import com.sap.sailing.domain.maneuverdetection.impl.ManeuverDetectorWithEstimationDataSupportDecoratorImpl;
import com.sap.sailing.domain.maneuverdetection.impl.TrackTimeInfo;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.CompleteManeuverCurve;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
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

    public static List<CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData>> getCompetitorTracksWithEstimationData(TrackedRace trackedRace,
            PolarDataService polarDataService) {
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
                BoatClass boatClass = trackedRace.getBoatOfCompetitor(competitor).getBoatClass();
                GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
                Distance distanceTravelled = track.getDistanceTraveled(trackTimeInfo.getTrackStartTimePoint(),
                        trackTimeInfo.getTrackEndTimePoint());
                double avgIntervalBetweenFixesInSeconds = track.getAverageIntervalBetweenFixes().asSeconds();
                CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData> competitorTrackWithEstimationData = new CompetitorTrackWithEstimationData<>(
                        competitor.getName(), boatClass, completeManeuverCurvesWithEstimationData,
                        avgIntervalBetweenFixesInSeconds, distanceTravelled, trackTimeInfo.getTrackStartTimePoint(),
                        trackTimeInfo.getTrackEndTimePoint(),
                        getFixesCountForPolars(polarDataService, trackedRace, competitor),
                        getMarkPassingsCount(trackedRace, competitor), getWaypointsCount(trackedRace));
                competitorTracks.add(competitorTrackWithEstimationData);
            }

        }
        return competitorTracks;
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

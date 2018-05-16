package com.sap.sailing.windestimation.data;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.maneuverdetection.impl.ManeuverDetectorImpl;
import com.sap.sailing.domain.maneuverdetection.impl.TrackTimeInfo;
import com.sap.sailing.domain.tracking.CompleteManeuverCurve;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedRace;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class EstimationDataUtil {

    private EstimationDataUtil() {
    }

    public static List<CompetitorTrackWithEstimationData> getCompetitorTracksWithEstimationData(
            TrackedRace trackedRace) {
        // TODO make iterative
        Iterable<Competitor> competitors = trackedRace.getRace().getCompetitors();
        ManeuverDetectorImpl maneuverDetector = new ManeuverDetectorImpl();
        List<CompetitorTrackWithEstimationData> competitorTracks = new ArrayList<>();
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
                CompetitorTrackWithEstimationData competitorTrackWithEstimationData = new CompetitorTrackWithEstimationData(
                        competitor.getName(), boatClass, completeManeuverCurvesWithEstimationData,
                        avgIntervalBetweenFixesInSeconds, distanceTravelled, trackTimeInfo.getTrackStartTimePoint(),
                        trackTimeInfo.getTrackEndTimePoint());
                competitorTracks.add(competitorTrackWithEstimationData);
            }

        }
        return competitorTracks;
    }

}

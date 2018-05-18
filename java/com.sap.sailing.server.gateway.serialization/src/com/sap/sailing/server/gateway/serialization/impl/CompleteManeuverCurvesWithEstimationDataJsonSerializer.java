package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.domain.maneuverdetection.ManeuverDetector;
import com.sap.sailing.domain.maneuverdetection.impl.ManeuverDetectorImpl;
import com.sap.sailing.domain.maneuverdetection.impl.TrackTimeInfo;
import com.sap.sailing.domain.tracking.CompleteManeuverCurve;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Duration;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class CompleteManeuverCurvesWithEstimationDataJsonSerializer extends AbstractTrackedRaceDataJsonSerializer {
    public static final String MANEUVER_CURVES = "maneuverCurves";
    public static final String BOAT_CLASS = "boatClass";
    public static final String COMPETITOR_NAME = "competitorName";
    public static final String AVG_INTERVAL_BETWEEN_FIXES_IN_SECONDS = "avgIntervalBetweenFixesInSeconds";
    public static final String DISTANCE_TRAVELLED_IN_METERS = "distanceTravelledInMeters";
    public static final String START_TIME_POINT = "startUnixTime";
    public static final String END_TIME_POINT = "endUnixTime";

    private final BoatClassJsonSerializer boatClassJsonSerializer;
    private final CompleteManeuverCurveWithEstimationDataJsonSerializer maneuverWithEstimationDataJsonSerializer;

    public CompleteManeuverCurvesWithEstimationDataJsonSerializer(BoatClassJsonSerializer boatClassJsonSerializer,
            CompleteManeuverCurveWithEstimationDataJsonSerializer maneuverWithEstimationDataJsonSerializer) {
        this.boatClassJsonSerializer = boatClassJsonSerializer;
        this.maneuverWithEstimationDataJsonSerializer = maneuverWithEstimationDataJsonSerializer;
    }

    @Override
    public JSONObject serialize(TrackedRace trackedRace) {
        final JSONObject result = new JSONObject();
        JSONArray byCompetitorJson = new JSONArray();
        result.put(BYCOMPETITOR, byCompetitorJson);
        for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
            ManeuverDetectorImpl maneuverDetector = new ManeuverDetectorImpl(trackedRace, competitor);
            TrackTimeInfo trackTimeInfo = maneuverDetector.getTrackTimeInfo();
            if (trackTimeInfo != null) {
                final JSONObject forCompetitorJson = new JSONObject();
                byCompetitorJson.add(forCompetitorJson);
                forCompetitorJson.put(COMPETITOR_NAME, competitor.getName());
                forCompetitorJson.put(BOAT_CLASS, boatClassJsonSerializer
                        .serialize(trackedRace.getRace().getBoatOfCompetitor(competitor).getBoatClass()));
                final JSONArray completeManeuverCurvesWithEstimationData = new JSONArray();
                for (CompleteManeuverCurveWithEstimationData maneuver : getCompleteManeuverCurvesWithEstimationData(
                        trackedRace, competitor)) {
                    completeManeuverCurvesWithEstimationData
                            .add(maneuverWithEstimationDataJsonSerializer.serialize(maneuver));
                }
                forCompetitorJson.put(MANEUVER_CURVES, completeManeuverCurvesWithEstimationData);
                Duration averageIntervalBetweenFixes = trackedRace.getTrack(competitor)
                        .getAverageIntervalBetweenFixes();
                forCompetitorJson.put(AVG_INTERVAL_BETWEEN_FIXES_IN_SECONDS,
                        averageIntervalBetweenFixes == null ? 0 : averageIntervalBetweenFixes.asSeconds());
                GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
                Double distanceTravelledInMeters = null;
                if (trackTimeInfo.getTrackStartTimePoint() != null && trackTimeInfo.getTrackEndTimePoint() != null) {
                    distanceTravelledInMeters = track.getDistanceTraveled(trackTimeInfo.getTrackStartTimePoint(),
                            trackTimeInfo.getTrackEndTimePoint()).getMeters();
                }
                forCompetitorJson.put(DISTANCE_TRAVELLED_IN_METERS, distanceTravelledInMeters);
                forCompetitorJson.put(START_TIME_POINT, trackTimeInfo.getTrackStartTimePoint() == null ? null
                        : trackTimeInfo.getTrackStartTimePoint().asMillis());
                forCompetitorJson.put(END_TIME_POINT, trackTimeInfo.getTrackEndTimePoint() == null ? null
                        : trackTimeInfo.getTrackEndTimePoint().asMillis());
            }
        }
        return result;
    }

    private Iterable<CompleteManeuverCurveWithEstimationData> getCompleteManeuverCurvesWithEstimationData(
            TrackedRace trackedRace, Competitor competitor) {
        Iterable<Maneuver> maneuvers = trackedRace.getManeuvers(competitor, false);
        ManeuverDetector maneuverDetector = new ManeuverDetectorImpl(trackedRace, competitor);
        Iterable<CompleteManeuverCurveWithEstimationData> maneuversWithEstimationData = null;
        try {
            Iterable<CompleteManeuverCurve> maneuverCurves = maneuverDetector.getCompleteManeuverCurves(maneuvers);
            maneuversWithEstimationData = maneuverDetector.getCompleteManeuverCurvesWithEstimationData(maneuverCurves);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return maneuversWithEstimationData;
    }

}

package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.maneuverdetection.impl.ManeuverDetectorImpl;
import com.sap.sailing.domain.maneuverdetection.impl.ManeuverDetectorWithEstimationDataSupportDecoratorImpl;
import com.sap.sailing.domain.maneuverdetection.impl.TrackTimeInfo;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsDurationImpl;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class GpsFixesWithEstimationDataJsonSerializer extends AbstractTrackedRaceDataJsonSerializer {
    public static final String GPS_FIXES = "gpsFixes";
    public static final String BOAT_CLASS = "boatClass";
    public static final String COMPETITOR_NAME = "competitorName";
    public static final String AVG_INTERVAL_BETWEEN_FIXES_IN_SECONDS = "avgIntervalBetweenFixesInSeconds";
    public static final String DISTANCE_TRAVELLED_IN_METERS = "distanceTravelledInMeters";
    public static final String START_TIME_POINT = "startUnixTime";
    public static final String END_TIME_POINT = "endUnixTime";
    public static final String WIND = "wind";
    public static final String RELATIVE_BEARING_TO_NEXT_MARK = "relativeBearingToNextMark";
    public static final String CLOSEST_DISTANCE_TO_MARK = "closestDistanceToMarkInMeters";

    private final BoatClassJsonSerializer boatClassJsonSerializer;
    private final GPSFixMovingJsonSerializer gpsFixMovingJsonSerializer;
    private final boolean addWind;
    private final boolean addNextWaypoint;
    private final ManeuverWindJsonSerializer windJsonSerializer;
    private final Boolean smoothFixes;
    private Integer startBeforeStartLineInSeconds;
    private Integer endBeforeStartLineInSeconds;
    private Integer startAfterFinishLineInSeconds;
    private Integer endAfterFinishLineInSeconds;

    public GpsFixesWithEstimationDataJsonSerializer(BoatClassJsonSerializer boatClassJsonSerializer,
            GPSFixMovingJsonSerializer gpsFixMovingJsonSerializer, ManeuverWindJsonSerializer windJsonSerializer,
            boolean addWind, boolean addNextWaypoint, Boolean smoothFixes, Integer startBeforeStartLineInSeconds,
            Integer endBeforeStartLineInSeconds, Integer startAfterFinishLineInSeconds,
            Integer endAfterFinishLineInSeconds) {
        this.boatClassJsonSerializer = boatClassJsonSerializer;
        this.gpsFixMovingJsonSerializer = gpsFixMovingJsonSerializer;
        this.windJsonSerializer = windJsonSerializer;
        this.addWind = addWind;
        this.addNextWaypoint = addNextWaypoint;
        this.smoothFixes = smoothFixes;
        this.startBeforeStartLineInSeconds = startBeforeStartLineInSeconds;
        this.endBeforeStartLineInSeconds = endBeforeStartLineInSeconds;
        this.startAfterFinishLineInSeconds = startAfterFinishLineInSeconds;
        this.endAfterFinishLineInSeconds = endAfterFinishLineInSeconds;
    }

    @Override
    public JSONObject serialize(TrackedRace trackedRace) {
        final JSONObject result = new JSONObject();
        JSONArray byCompetitorJson = new JSONArray();
        result.put(BYCOMPETITOR, byCompetitorJson);
        for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
            ManeuverDetectorImpl maneuverDetector = new ManeuverDetectorImpl(trackedRace, competitor);
            ManeuverDetectorWithEstimationDataSupportDecoratorImpl estimationDataSupportDecoratorImpl = new ManeuverDetectorWithEstimationDataSupportDecoratorImpl(
                    maneuverDetector, null);
            TrackTimeInfo trackTimeInfo = maneuverDetector.getTrackTimeInfo();
            TimePoint from = null;
            TimePoint to = null;
            if (startBeforeStartLineInSeconds != Integer.MIN_VALUE) {
                from = trackTimeInfo.getTrackStartTimePoint()
                        .minus(new MillisecondsDurationImpl(startBeforeStartLineInSeconds * 1000L));
            } else if (startAfterFinishLineInSeconds != Integer.MIN_VALUE) {
                from = trackTimeInfo.getTrackEndTimePoint()
                        .plus(new MillisecondsDurationImpl(startAfterFinishLineInSeconds * 1000L));
            } else {
                from = trackTimeInfo.getTrackStartTimePoint();
            }
            if (endAfterFinishLineInSeconds != Integer.MIN_VALUE) {
                to = trackTimeInfo.getTrackEndTimePoint()
                        .plus(new MillisecondsDurationImpl(endAfterFinishLineInSeconds * 1000L));
            } else if (endBeforeStartLineInSeconds != Integer.MIN_VALUE) {
                to = trackTimeInfo.getTrackStartTimePoint()
                        .minus(new MillisecondsDurationImpl(endBeforeStartLineInSeconds * 1000L));
            } else {
                to = trackTimeInfo.getTrackEndTimePoint();
            }
            if (trackTimeInfo != null) {
                final JSONObject forCompetitorJson = new JSONObject();
                byCompetitorJson.add(forCompetitorJson);
                forCompetitorJson.put(COMPETITOR_NAME, competitor.getName());
                forCompetitorJson.put(BOAT_CLASS, boatClassJsonSerializer
                        .serialize(trackedRace.getRace().getBoatOfCompetitor(competitor).getBoatClass()));
                final JSONArray gpsFixesWithEstimationData = new JSONArray();
                GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
                track.lockForRead();
                try {
                    for (GPSFixMoving gpsFix : track.getFixes(from, true, to, true)) {
                        JSONObject serializedGpsFix = gpsFixMovingJsonSerializer.serialize(gpsFix);
                        if (addWind) {
                            Wind wind = trackedRace.getWind(gpsFix.getPosition(), gpsFix.getTimePoint());
                            JSONObject serializedWind = wind == null ? null : windJsonSerializer.serialize(wind);
                            serializedGpsFix.put(WIND, serializedWind);
                        }
                        if (addNextWaypoint) {
                            Distance closestDistanceToMark = estimationDataSupportDecoratorImpl
                                    .getClosestDistanceToMark(gpsFix.getTimePoint());
                            SpeedWithBearing speedWithBearing = smoothFixes
                                    ? track.getEstimatedSpeed(gpsFix.getTimePoint()) : gpsFix.getSpeed();
                            Bearing relativeBearingToNextMark = speedWithBearing == null ? null
                                    : estimationDataSupportDecoratorImpl.getRelativeBearingToNextMark(
                                            gpsFix.getTimePoint(), speedWithBearing.getBearing());
                            serializedGpsFix.put(CLOSEST_DISTANCE_TO_MARK,
                                    closestDistanceToMark == null ? null : closestDistanceToMark.getMeters());
                            serializedGpsFix.put(RELATIVE_BEARING_TO_NEXT_MARK,
                                    relativeBearingToNextMark == null ? null : relativeBearingToNextMark.getDegrees());
                        }
                        gpsFixesWithEstimationData.add(serializedGpsFix);
                    }
                } finally {
                    track.unlockAfterRead();
                }
                forCompetitorJson.put(GPS_FIXES, gpsFixesWithEstimationData);
                Duration averageIntervalBetweenFixes = trackedRace.getTrack(competitor)
                        .getAverageIntervalBetweenFixes();
                forCompetitorJson.put(AVG_INTERVAL_BETWEEN_FIXES_IN_SECONDS,
                        averageIntervalBetweenFixes == null ? 0 : averageIntervalBetweenFixes.asSeconds());
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

}

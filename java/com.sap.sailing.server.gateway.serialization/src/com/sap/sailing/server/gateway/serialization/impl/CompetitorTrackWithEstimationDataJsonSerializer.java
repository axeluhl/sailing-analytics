package com.sap.sailing.server.gateway.serialization.impl;

import java.util.NavigableSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.maneuverdetection.impl.ManeuverDetectorImpl;
import com.sap.sailing.domain.maneuverdetection.impl.TrackTimeInfo;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Duration;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.security.SecurityService;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class CompetitorTrackWithEstimationDataJsonSerializer extends AbstractTrackedRaceDataJsonSerializer {
    public static final String ELEMENTS = "elements";
    public static final String BOAT_CLASS = "boatClass";
    public static final String COMPETITOR_NAME = "competitorName";
    public static final String AVG_INTERVAL_BETWEEN_FIXES_IN_SECONDS = "avgIntervalBetweenFixesInSeconds";
    public static final String DISTANCE_TRAVELLED_IN_METERS = "distanceTravelledInMeters";
    public static final String START_TIME_POINT = "startUnixTime";
    public static final String END_TIME_POINT = "endUnixTime";
    public static final String FIXES_COUNT_FOR_POLARS = "fixesCountForPolars";
    public static final String MARK_PASSINGS_COUNT = "markPassingsCount";
    public static final String WAYPOINTS_COUNT = "waypointsCount";
    public static final String WIND_QUALITY = "windQuality";

    private final BoatClassJsonSerializer boatClassJsonSerializer;
    private final CompetitorTrackElementsJsonSerializer elementsJsonSerializer;
    private final PolarDataService polarDataService;
    private final SecurityService securityService;
    private final Integer startBeforeStartLineInSeconds;
    private final Integer endBeforeStartLineInSeconds;
    private final Integer startAfterFinishLineInSeconds;
    private final Integer endAfterFinishLineInSeconds;

    public CompetitorTrackWithEstimationDataJsonSerializer(PolarDataService polarDataService, SecurityService securityService,
            BoatClassJsonSerializer boatClassJsonSerializer,
            CompetitorTrackElementsJsonSerializer elementsJsonSerializer, Integer startBeforeStartLineInSeconds,
            Integer endBeforeStartLineInSeconds, Integer startAfterFinishLineInSeconds,
            Integer endAfterFinishLineInSeconds) {
        this.polarDataService = polarDataService;
        this.boatClassJsonSerializer = boatClassJsonSerializer;
        this.elementsJsonSerializer = elementsJsonSerializer;
        this.startBeforeStartLineInSeconds = startBeforeStartLineInSeconds;
        this.endBeforeStartLineInSeconds = endBeforeStartLineInSeconds;
        this.startAfterFinishLineInSeconds = startAfterFinishLineInSeconds;
        this.endAfterFinishLineInSeconds = endAfterFinishLineInSeconds;
        this.securityService = securityService;
    }

    @Override
    public JSONObject serialize(TrackedRace trackedRace) {
        final JSONObject result = new JSONObject();
        JSONArray byCompetitorJson = new JSONArray();
        int windQuality = 0;
        for (WindSource windSource : trackedRace.getWindSources()) {
            if (windSource.getType() == WindSourceType.EXPEDITION) {
                windQuality = 2;
                break;
            }
            if (windSource.getType() == WindSourceType.RACECOMMITTEE) {
                windQuality = 1;
            }
        }
        result.put(WIND_QUALITY, windQuality);
        result.put(BYCOMPETITOR, byCompetitorJson);
        for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
            if (securityService.hasCurrentUserExplictPermissions(competitor,
                    SecuredDomainType.CompetitorAndBoatActions.READ_PUBLIC)) {
                ManeuverDetectorImpl maneuverDetector = new ManeuverDetectorImpl(trackedRace, competitor);
                TrackTimeInfo trackTimeInfo = maneuverDetector.getTrackTimeInfo();
                if (trackTimeInfo != null) {
                    TimePoint from = null;
                    TimePoint to = null;
                    if (startBeforeStartLineInSeconds != null) {
                        from = trackTimeInfo.getTrackStartTimePoint()
                                .minus(new MillisecondsDurationImpl(startBeforeStartLineInSeconds * 1000L));
                    } else if (startAfterFinishLineInSeconds != null) {
                        from = trackTimeInfo.getTrackEndTimePoint()
                                .plus(new MillisecondsDurationImpl(startAfterFinishLineInSeconds * 1000L));
                    } else {
                        from = trackTimeInfo.getTrackStartTimePoint();
                    }
                    if (endAfterFinishLineInSeconds != null) {
                        to = trackTimeInfo.getTrackEndTimePoint()
                                .plus(new MillisecondsDurationImpl(endAfterFinishLineInSeconds * 1000L));
                    } else if (endBeforeStartLineInSeconds != null) {
                        to = trackTimeInfo.getTrackStartTimePoint()
                                .minus(new MillisecondsDurationImpl(endBeforeStartLineInSeconds * 1000L));
                    } else {
                        to = trackTimeInfo.getTrackEndTimePoint();
                    }
                    final JSONObject forCompetitorJson = new JSONObject();
                    byCompetitorJson.add(forCompetitorJson);
                    forCompetitorJson.put(COMPETITOR_NAME, competitor.getName());
                    forCompetitorJson.put(BOAT_CLASS, boatClassJsonSerializer
                            .serialize(trackedRace.getRace().getBoatOfCompetitor(competitor).getBoatClass()));
                    forCompetitorJson.put(FIXES_COUNT_FOR_POLARS, getFixesCountForPolars(trackedRace, competitor));
                    Duration averageIntervalBetweenFixes = trackedRace.getTrack(competitor)
                            .getAverageIntervalBetweenFixes();
                    forCompetitorJson.put(AVG_INTERVAL_BETWEEN_FIXES_IN_SECONDS,
                            averageIntervalBetweenFixes == null ? 0 : averageIntervalBetweenFixes.asSeconds());
                    GPSFixTrack<Competitor, GPSFixMoving> track = trackedRace.getTrack(competitor);
                    Double distanceTravelledInMeters = null;
                    if (trackTimeInfo.getTrackStartTimePoint() != null
                            && trackTimeInfo.getTrackEndTimePoint() != null) {
                        distanceTravelledInMeters = track.getDistanceTraveled(trackTimeInfo.getTrackStartTimePoint(),
                                trackTimeInfo.getTrackEndTimePoint()).getMeters();
                    }
                    forCompetitorJson.put(DISTANCE_TRAVELLED_IN_METERS, distanceTravelledInMeters);
                    forCompetitorJson.put(START_TIME_POINT, trackTimeInfo.getTrackStartTimePoint() == null ? null
                            : trackTimeInfo.getTrackStartTimePoint().asMillis());
                    forCompetitorJson.put(END_TIME_POINT, trackTimeInfo.getTrackEndTimePoint() == null ? null
                            : trackTimeInfo.getTrackEndTimePoint().asMillis());
                    forCompetitorJson.put(MARK_PASSINGS_COUNT, getMarkPassingsCount(trackedRace, competitor));
                    forCompetitorJson.put(WAYPOINTS_COUNT, getWaypointsCount(trackedRace));
                    forCompetitorJson.put(ELEMENTS,
                            elementsJsonSerializer.serialize(trackedRace, competitor, from, to, trackTimeInfo));
                }
            }
        }
        return result;
    }

    private int getMarkPassingsCount(TrackedRace trackedRace, Competitor competitor) {
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

    private long getFixesCountForPolars(TrackedRace trackedRace, Competitor competitor) {
        BoatClass boatClass = trackedRace.getRace().getBoatOfCompetitor(competitor).getBoatClass();
        Long fixesCountForBoatPolars = polarDataService.getFixCountPerBoatClass().get(boatClass);
        return fixesCountForBoatPolars == null ? 0L : fixesCountForBoatPolars;
    }

    private int getWaypointsCount(TrackedRace trackedRace) {
        Iterable<Waypoint> waypoints = trackedRace.getRace().getCourse().getWaypoints();
        return Util.size(waypoints);
    }

}

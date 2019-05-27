package com.sap.sailing.server.gateway.serialization.impl;

import java.text.SimpleDateFormat;
import java.util.Map.Entry;
import java.util.NavigableSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.Util.Pair;

public class MarkPassingsJsonSerializer extends AbstractTrackedRaceDataJsonSerializer {
    public static final String ZERO_BASED_WAYPOINT_INDEX = "zeroBasedWaypointIndex";
    public static final String WAYPOINT_NAME = "waypointName";
    public static final String BYWAYPOINT = "bywaypoint";
    public static final String MARKPASSINGS = "markpassings";
    private static SimpleDateFormat TIMEPOINT_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @Override
    public JSONObject serialize(TrackedRace trackedRace) {
        final Course course = trackedRace.getRace().getCourse();
        JSONObject result = new JSONObject();
        CompetitorAndBoatJsonSerializer competitorWithBoatSerializer = CompetitorAndBoatJsonSerializer.create(/* serializeNonPublicCompetitorFields */ false);
        CompetitorJsonSerializer competitorSerializer = CompetitorJsonSerializer.create();
        JSONArray byCompetitorJson = new JSONArray();
        result.put(BYCOMPETITOR, byCompetitorJson);
        for (Entry<Competitor, Boat> competitorAndBoatEntry : trackedRace.getRace().getCompetitorsAndTheirBoats().entrySet()) {
            Competitor competitor = competitorAndBoatEntry.getKey();
            Boat boat = competitorAndBoatEntry.getValue();
            JSONObject forCompetitorJson = new JSONObject();
            byCompetitorJson.add(forCompetitorJson);
            forCompetitorJson.put(COMPETITOR, competitorWithBoatSerializer.serialize(new Pair<>(competitor, boat)));
            final NavigableSet<MarkPassing> markPassingsForCompetitor = trackedRace.getMarkPassings(competitor);
            JSONArray markPassingsForCompetitorJson = new JSONArray();
            forCompetitorJson.put(MARKPASSINGS, markPassingsForCompetitorJson);
            trackedRace.lockForRead(markPassingsForCompetitor);
            try {
                for (MarkPassing markPassing : markPassingsForCompetitor) {
                    JSONObject markPassingJson = new JSONObject();
                    markPassingsForCompetitorJson.add(markPassingJson);
                    addWaypoint(course, markPassing.getWaypoint(), markPassingJson);
                    addMarkPassingTime(markPassing, markPassingJson);
                }
            } finally {
                trackedRace.unlockAfterRead(markPassingsForCompetitor);
            }
        }
        JSONArray byWaypointJson = new JSONArray();
        result.put(BYWAYPOINT, byWaypointJson);
        for (Waypoint waypoint : course.getWaypoints()) {
            JSONObject jsonForWaypoint = new JSONObject();
            byWaypointJson.add(jsonForWaypoint);
            addWaypoint(course, waypoint, jsonForWaypoint);
            final Iterable<MarkPassing> markPassingsForWaypoint = trackedRace.getMarkPassingsInOrder(waypoint);
            trackedRace.lockForRead(markPassingsForWaypoint);
            try {
                JSONArray markPassingsForWaypointJson = new JSONArray();
                jsonForWaypoint.put(MARKPASSINGS, markPassingsForWaypointJson);
                for (MarkPassing markPassing : markPassingsForWaypoint) {
                    JSONObject markPassingJson = new JSONObject();
                    markPassingsForWaypointJson.add(markPassingJson);
                    markPassingJson.put(COMPETITOR, competitorSerializer.serialize(markPassing.getCompetitor()));
                    addMarkPassingTime(markPassing, markPassingJson);
                }
            } finally {
                trackedRace.unlockAfterRead(markPassingsForWaypoint);
            }
        }
        return result;
    }

    private void addMarkPassingTime(MarkPassing markPassing, JSONObject markPassingJson) {
        markPassingJson.put(TIMEASMILLIS, markPassing.getTimePoint().asMillis());
        markPassingJson.put(TIMEASISO, TIMEPOINT_FORMATTER.format(markPassing.getTimePoint().asDate()));
    }

    private void addWaypoint(final Course course, Waypoint waypoint, JSONObject jsonToAddTo) {
        jsonToAddTo.put(WAYPOINT_NAME, waypoint.getName());
        jsonToAddTo.put(ZERO_BASED_WAYPOINT_INDEX, course.getIndexOfWaypoint(waypoint));
    }
}

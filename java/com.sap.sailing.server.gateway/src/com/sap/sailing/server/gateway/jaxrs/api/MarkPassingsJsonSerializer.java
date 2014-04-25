package com.sap.sailing.server.gateway.jaxrs.api;

import java.text.SimpleDateFormat;
import java.util.NavigableSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Course;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.BoatJsonSerializer;
import com.sap.sailing.server.gateway.serialization.impl.CompetitorJsonSerializer;

public class MarkPassingsJsonSerializer implements JsonSerializer<TrackedRace> {
    private static SimpleDateFormat TIMEPOINT_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @Override
    public JSONObject serialize(TrackedRace trackedRace) {
        final Course course = trackedRace.getRace().getCourse();
        JSONObject result = new JSONObject();
        CompetitorJsonSerializer competitorSerializer = new CompetitorJsonSerializer(null, BoatJsonSerializer.create());
        JSONArray byCompetitorJson = new JSONArray();
        result.put("bycompetitor", byCompetitorJson);
        for (Competitor competitor : trackedRace.getRace().getCompetitors()) {
            JSONObject forCompetitorJson = new JSONObject();
            byCompetitorJson.add(forCompetitorJson);
            forCompetitorJson.put("competitor", competitorSerializer.serialize(competitor));
            final NavigableSet<MarkPassing> markPassingsForCompetitor = trackedRace.getMarkPassings(competitor);
            JSONArray markPassingsForCompetitorJson = new JSONArray();
            forCompetitorJson.put("markpassings", markPassingsForCompetitorJson);
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
        result.put("bywaypoint", byWaypointJson);
        for (Waypoint waypoint : course.getWaypoints()) {
            JSONObject jsonForWaypoint = new JSONObject();
            byWaypointJson.add(jsonForWaypoint);
            addWaypoint(course, waypoint, jsonForWaypoint);
            final Iterable<MarkPassing> markPassingsForWaypoint = trackedRace.getMarkPassingsInOrder(waypoint);
            trackedRace.lockForRead(markPassingsForWaypoint);
            try {
                JSONArray markPassingsForWaypointJson = new JSONArray();
                jsonForWaypoint.put("markpassings", markPassingsForWaypointJson);
                for (MarkPassing markPassing : markPassingsForWaypoint) {
                    JSONObject markPassingJson = new JSONObject();
                    markPassingsForWaypointJson.add(markPassingJson);
                    markPassingJson.put("competitor", competitorSerializer.serialize(markPassing.getCompetitor()));
                    addMarkPassingTime(markPassing, markPassingJson);
                }
            } finally {
                trackedRace.unlockAfterRead(markPassingsForWaypoint);
            }
        }
        return result;
    }

    private void addMarkPassingTime(MarkPassing markPassing, JSONObject markPassingJson) {
        markPassingJson.put("timeasmillis", markPassing.getTimePoint().asMillis());
        markPassingJson.put("timeasiso", TIMEPOINT_FORMATTER.format(markPassing.getTimePoint().asDate()));
    }

    private void addWaypoint(final Course course, Waypoint waypoint, JSONObject jsonToAddTo) {
        jsonToAddTo.put("waypointName", waypoint.getName());
        jsonToAddTo.put("zeroBasedWaypointIndex", course.getIndexOfWaypoint(waypoint));
    }
}

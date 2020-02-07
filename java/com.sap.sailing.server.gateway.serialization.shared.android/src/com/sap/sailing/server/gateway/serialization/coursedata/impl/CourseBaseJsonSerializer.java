package com.sap.sailing.server.gateway.serialization.coursedata.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class CourseBaseJsonSerializer implements JsonSerializer<CourseBase> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_WAYPOINTS = "waypoints";
    public static final String FIELD_ORIGINATING_COURSE_TEMPLATE_ID = "originatingCourseTemplateId";

    private final JsonSerializer<Waypoint> waypointSerializer;

    public CourseBaseJsonSerializer(JsonSerializer<Waypoint> waypointSerializer) {
        this.waypointSerializer = waypointSerializer;
    }

    @Override
    public JSONObject serialize(CourseBase course) {
        JSONObject result = new JSONObject();
        result.put(FIELD_NAME, course.getName());
        if (course.getOriginatingCourseTemplateIdOrNull() != null) {
            result.put(FIELD_ORIGINATING_COURSE_TEMPLATE_ID, course.getOriginatingCourseTemplateIdOrNull());
        }
        JSONArray waypoints = new JSONArray();
        for (Waypoint waypoint : course.getWaypoints()) {
            waypoints.add(serializeWaypoint(waypoint));
        }
        result.put(FIELD_WAYPOINTS, waypoints);
        return result;
    }

    protected JSONObject serializeWaypoint(Waypoint waypoint) {
        return waypointSerializer.serialize(waypoint);
    }
}

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
    public JSONObject serialize(CourseBase object) {
        JSONObject result = new JSONObject();

        result.put(FIELD_NAME, object.getName());
        if (object.getOriginatingCourseTemplateIdOrNull() != null) {
            result.put(FIELD_ORIGINATING_COURSE_TEMPLATE_ID, object.getOriginatingCourseTemplateIdOrNull());
        }

        JSONArray waypoints = new JSONArray();
        for (Waypoint waypoint : object.getWaypoints()) {
            waypoints.add(waypointSerializer.serialize(waypoint));
        }
        result.put(FIELD_WAYPOINTS, waypoints);

        return result;
    }

}

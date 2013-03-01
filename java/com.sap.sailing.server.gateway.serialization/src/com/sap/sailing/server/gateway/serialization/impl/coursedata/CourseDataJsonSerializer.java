package com.sap.sailing.server.gateway.serialization.impl.coursedata;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.CourseData;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class CourseDataJsonSerializer implements JsonSerializer<CourseData> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_WAYPOINTS = "waypoints";

    private final JsonSerializer<Waypoint> waypointSerializer;

    public CourseDataJsonSerializer(JsonSerializer<Waypoint> waypointSerializer) {
        this.waypointSerializer = waypointSerializer;
    }

    @Override
    public JSONObject serialize(CourseData object) {
        JSONObject result = new JSONObject();

        result.put(FIELD_NAME, object.getName());

        JSONArray waypoints = new JSONArray();
        for (Waypoint waypoint : object.getWaypoints()) {
            waypoints.add(waypointSerializer.serialize(waypoint));
        }
        result.put(FIELD_WAYPOINTS, waypoints);

        return result;
    }

}

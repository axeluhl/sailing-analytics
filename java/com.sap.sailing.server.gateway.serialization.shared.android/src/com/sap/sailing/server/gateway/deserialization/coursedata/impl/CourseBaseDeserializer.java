package com.sap.sailing.server.gateway.deserialization.coursedata.impl;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.base.impl.CourseDataImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.coursedata.impl.CourseBaseJsonSerializer;

/**
 * Deserializer for CourseData.
 */
public class CourseBaseDeserializer implements JsonDeserializer<CourseBase> {

    private final WaypointDeserializer waypointDeserializer;

    public CourseBaseDeserializer(WaypointDeserializer waypointDeserializer) {
        this.waypointDeserializer = waypointDeserializer;
    }

    @Override
    public CourseBase deserialize(JSONObject object) throws JsonDeserializationException {
        String courseName = (String) object.get(CourseBaseJsonSerializer.FIELD_NAME);
        JSONArray jsonWaypoints = (JSONArray) object.get(CourseBaseJsonSerializer.FIELD_WAYPOINTS);
        List<Waypoint> waypoints = new ArrayList<Waypoint>();
        
        for (Object waypointObject : jsonWaypoints) {
            JSONObject jsonWaypoint = (JSONObject) waypointObject;
            Waypoint waypoint = waypointDeserializer.deserialize(jsonWaypoint);
            waypoints.add(waypoint);
        }
        
        CourseBase courseBase = new CourseDataImpl(courseName);
        for (int i = 0; i < waypoints.size(); i++) {
            courseBase.addWaypoint(i, waypoints.get(i));
        }
        
        return courseBase;
    }

}
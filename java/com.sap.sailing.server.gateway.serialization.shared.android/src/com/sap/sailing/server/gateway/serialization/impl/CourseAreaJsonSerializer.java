package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sse.shared.json.JsonSerializer;

public class CourseAreaJsonSerializer implements JsonSerializer<CourseArea> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_ID = "id";
    public static final String FIELD_RACES = "races";
    public static final String FIELD_CENTER_POSITION = "centerPosition";
    public static final String FIELD_RADIUS_IN_METERS = "radiusInMeters";


    @Override
    public JSONObject serialize(CourseArea object) {
        JSONObject result = new JSONObject();
        result.put(FIELD_NAME, object.getName());
        result.put(FIELD_ID, object.getId().toString());
        if (object.getCenterPosition() != null) {
            result.put(FIELD_CENTER_POSITION, new PositionJsonSerializer().serialize(object.getCenterPosition()));
        }
        if (object.getRadius() != null) {
            result.put(FIELD_RADIUS_IN_METERS, object.getRadius().getMeters());
        }
        return result;
    }
}

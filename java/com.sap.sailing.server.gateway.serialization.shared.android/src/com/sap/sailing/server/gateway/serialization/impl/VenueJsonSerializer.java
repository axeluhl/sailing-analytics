package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.CourseArea;
import com.sap.sailing.domain.base.Venue;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class VenueJsonSerializer implements JsonSerializer<Venue> {
    public static final String FIELD_NAME = "name";
    public static final String FIELD_COURSE_AREAS = "courseAreas";

    private final JsonSerializer<CourseArea> areaSerializer;

    public VenueJsonSerializer(JsonSerializer<CourseArea> areaSerializer) {
        this.areaSerializer = areaSerializer;
    }

    @Override
    public JSONObject serialize(Venue venue) {
        JSONObject result = new JSONObject();

        result.put(FIELD_NAME, venue.getName());
        JSONArray areas = new JSONArray();
        for (CourseArea area : venue.getCourseAreas()) {
            areas.add(areaSerializer.serialize(area));
        }
        result.put(FIELD_COURSE_AREAS, areas);

        return result;
    }
}

package com.sap.sailing.server.gateway.serialization.coursedata.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.CourseData;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class CourseJsonSerializer implements JsonSerializer<CourseData> {
    public static final String FIELD_COURSE = "course";

    private final JsonSerializer<CourseData> courseDataSerializer;

    public CourseJsonSerializer(JsonSerializer<CourseData> courseDataSerializer) {
        this.courseDataSerializer = courseDataSerializer;
    }

    @Override
    public JSONObject serialize(CourseData object) {
        JSONObject result = new JSONObject();

        result.put(FIELD_COURSE, courseDataSerializer.serialize(object));

        return result;
    }

}

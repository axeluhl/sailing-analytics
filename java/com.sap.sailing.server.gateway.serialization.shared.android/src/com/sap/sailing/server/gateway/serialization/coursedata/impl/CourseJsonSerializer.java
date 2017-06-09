package com.sap.sailing.server.gateway.serialization.coursedata.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class CourseJsonSerializer implements JsonSerializer<CourseBase> {
    public static final String FIELD_COURSE = "course";

    private final JsonSerializer<CourseBase> courseBaseSerializer;

    public CourseJsonSerializer(JsonSerializer<CourseBase> courseBaseSerializer) {
        this.courseBaseSerializer = courseBaseSerializer;
    }

    @Override
    public JSONObject serialize(CourseBase object) {
        JSONObject result = new JSONObject();

        result.put(FIELD_COURSE, courseBaseSerializer.serialize(object));

        return result;
    }

}

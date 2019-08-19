package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class CourseTemplateJsonSerializer implements JsonSerializer<CourseTemplate> {

    private static final String FIELD_ID = "id";
    private static final String FIELD_NAME = "name";

    @Override
    public JSONObject serialize(CourseTemplate markProperties) {
        JSONObject result = new JSONObject();
        result.put(FIELD_ID, markProperties.getId().toString());
        result.put(FIELD_NAME, markProperties.getName());
        
        // TODO more...
        
        return result;
    }

}

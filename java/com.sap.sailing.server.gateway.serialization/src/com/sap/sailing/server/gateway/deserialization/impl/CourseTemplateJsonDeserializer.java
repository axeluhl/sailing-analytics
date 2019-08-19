package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.UUID;
import java.util.function.Function;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.CourseTemplate;
import com.sap.sailing.domain.coursetemplate.MarkTemplate;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;

public class CourseTemplateJsonDeserializer implements JsonDeserializer<CourseTemplate> {

    private final Function<UUID, MarkTemplate> markTemplateResolver;

    public CourseTemplateJsonDeserializer(Function<UUID, MarkTemplate> markTemplateResolver) {
        this.markTemplateResolver = markTemplateResolver;
    }

    @Override
    public CourseTemplate deserialize(JSONObject object) throws JsonDeserializationException {
        // TODO Auto-generated method stub
        return null;
    }
}

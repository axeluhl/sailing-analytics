package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.CourseConfiguration;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class CourseConfigurationJsonSerializer implements JsonSerializer<CourseConfiguration> {

    public static final String FIELD_NAME = "NAME";
    public static final String FIELD_MARK_CONFIGURATIONS = "markConfigurations";
    public static final String FIELD_MARK_CONFIGURATION_MARK_TEMPLATE_ID = "markTemplateId";
    public static final String FIELD_MARK_CONFIGURATION_MARK_PROPERTIES_ID = "markPropertiesId";
    public static final String FIELD_MARK_CONFIGURATION_MARK_ID = "markId";
    public static final String FIELD_MARK_CONFIGURATION_COMMON_MARK_PROPERTIES = "commonMarkProperties";

    public CourseConfigurationJsonSerializer() {
    }

    @Override
    public JSONObject serialize(CourseConfiguration object) {
        return null;
    }

}

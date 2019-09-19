package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.coursetemplate.CommonMarkProperties;
import com.sap.sailing.domain.coursetemplate.CourseConfiguration;
import com.sap.sailing.domain.coursetemplate.MarkConfiguration;
import com.sap.sailing.domain.sharedsailingdata.SharedSailingData;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.CourseConfigurationJsonSerializer;

public class CourseConfigurationJsonDeserializer implements JsonDeserializer<CourseConfiguration> {

    private final SharedSailingData sharedSailingData;
    private final CommonMarkPropertiesJsonDeserializer commonMarkPropertiesJsonDeserializer;

    public CourseConfigurationJsonDeserializer(final SharedSailingData sharedSailingData) {
        this.sharedSailingData = sharedSailingData;
        commonMarkPropertiesJsonDeserializer = new CommonMarkPropertiesJsonDeserializer();
    }

    @Override
    public CourseConfiguration deserialize(JSONObject json) throws JsonDeserializationException {
        final String courseConfigurationName = (String) json.get(CourseConfigurationJsonSerializer.FIELD_NAME);
        
        // TODO: how to get CourseConfigurationBuilder from com.sap.sailing.server.interfaces.coursetemplate (import leads in cyclic references???
        CourseConfigurationBuilder builder = new CourseConfigurationBuilder(sharedSailingData, null, null);
        
        final Map<Integer, MarkConfiguration> markConfigurations = new HashMap<Integer, MarkConfiguration>();
        final JSONArray allMarkConfigurationsJSON = (JSONArray) json.get(CourseConfigurationJsonSerializer.FIELD_MARK_CONFIGURATIONS);
        for (Object markConfigurationJSON : allMarkConfigurationsJSON) {
            final JSONObject markConfiguration = (JSONObject) markConfigurationJSON;
            final String markTemplateID = (String) markConfiguration.get(CourseConfigurationJsonSerializer.FIELD_MARK_CONFIGURATION_MARK_TEMPLATE_ID);
            final String markPropertiesID = (String) markConfiguration.get(CourseConfigurationJsonSerializer.FIELD_MARK_CONFIGURATION_MARK_PROPERTIES_ID);
            final String markID = (String) markConfiguration.get(CourseConfigurationJsonSerializer.FIELD_MARK_CONFIGURATION_MARK_ID);
            CommonMarkProperties commonMarkProperties = commonMarkPropertiesJsonDeserializer.deserialize(json);
            // TODO add optionalPositioning
            
            builder.addMarkConfiguration(UUID.fromString(markTemplateID), UUID.fromString(markPropertiesID), UUID.fromString(markID), commonMarkProperties, null);
        }
        
        return builder.build();
    }

}

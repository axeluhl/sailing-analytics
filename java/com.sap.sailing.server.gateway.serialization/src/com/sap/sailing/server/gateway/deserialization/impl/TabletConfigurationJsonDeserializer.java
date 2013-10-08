package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.TabletConfiguration;
import com.sap.sailing.domain.base.impl.TabletConfigurationImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;

public class TabletConfigurationJsonDeserializer implements JsonDeserializer<TabletConfiguration> {

    @Override
    public TabletConfiguration deserialize(JSONObject object) throws JsonDeserializationException {
        TabletConfigurationImpl configuration = new TabletConfigurationImpl();
        
        JSONArray courseAreaNames = Helpers.getNestedArraySafe(
                object, 
                "courseAreaNames");
        Set<String> allowedCourseAreaNames = new HashSet<String>();
        for (Object name : courseAreaNames) {
            allowedCourseAreaNames.add(name.toString());
        }
        configuration.setAllowedCourseAreaNames(allowedCourseAreaNames);
        
        
        return configuration;
    }

}

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

        if (object.containsKey("courseAreaNames")) {
            JSONArray courseAreaNames = Helpers.getNestedArraySafe(object, "courseAreaNames");
            Set<String> allowedCourseAreaNames = new HashSet<String>();
            for (Object name : courseAreaNames) {
                allowedCourseAreaNames.add(name.toString());
            }
            configuration.setAllowedCourseAreaNames(allowedCourseAreaNames);
        }

        if (object.containsKey("minRounds")) {
            Number minRounds = (Number) object.get("minRounds");
            configuration.setMinimumRoundsForCourse(minRounds.intValue());
        }

        if (object.containsKey("maxRounds")) {
            Number maxRounds = (Number) object.get("maxRounds");
            configuration.setMaximumRoundsForCourse(maxRounds.intValue());
        }

        if (object.containsKey("resultsRecipent")) {
            String resultsMailRecipent = (String) object.get("resultsRecipent");
            configuration.setResultsMailRecipent(resultsMailRecipent);
        }

        return configuration;
    }

}

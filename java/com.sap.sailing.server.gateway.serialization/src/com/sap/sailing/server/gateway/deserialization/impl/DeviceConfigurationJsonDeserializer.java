package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.DeviceConfigurationJsonSerializer;

public class DeviceConfigurationJsonDeserializer implements JsonDeserializer<DeviceConfiguration> {

    @Override
    public DeviceConfiguration deserialize(JSONObject object) throws JsonDeserializationException {
        DeviceConfigurationImpl configuration = createDeviceConfiguration();

        if (object.containsKey(DeviceConfigurationJsonSerializer.FIELD_COURSE_AREA_NAMES)) {
            JSONArray courseAreaNames = Helpers.getNestedArraySafe(object, DeviceConfigurationJsonSerializer.FIELD_COURSE_AREA_NAMES);
            List<String> allowedCourseAreaNames = new ArrayList<String>();
            for (Object name : courseAreaNames) {
                allowedCourseAreaNames.add(name.toString());
            }
            configuration.setAllowedCourseAreaNames(allowedCourseAreaNames);
        }

        if (object.containsKey(DeviceConfigurationJsonSerializer.FIELD_MIN_ROUNDS)) {
            Number minRounds = (Number) object.get(DeviceConfigurationJsonSerializer.FIELD_MIN_ROUNDS);
            configuration.setMinimumRoundsForCourse(minRounds.intValue());
        }

        if (object.containsKey(DeviceConfigurationJsonSerializer.FIELD_MAX_ROUNDS)) {
            Number maxRounds = (Number) object.get(DeviceConfigurationJsonSerializer.FIELD_MAX_ROUNDS);
            configuration.setMaximumRoundsForCourse(maxRounds.intValue());
        }

        if (object.containsKey(DeviceConfigurationJsonSerializer.FIELD_RESULTS_RECIPIENT)) {
            String resultsRecipient = (String) object.get(DeviceConfigurationJsonSerializer.FIELD_RESULTS_RECIPIENT);
            configuration.setResultsMailRecipient(resultsRecipient);
        }

        return configuration;
    }
    
    protected DeviceConfigurationImpl createDeviceConfiguration() {
        return new DeviceConfigurationImpl();
    }

}

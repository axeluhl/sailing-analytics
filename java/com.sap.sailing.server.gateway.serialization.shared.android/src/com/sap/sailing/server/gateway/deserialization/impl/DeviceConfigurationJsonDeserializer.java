package com.sap.sailing.server.gateway.deserialization.impl;

import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationImpl;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.DeviceConfigurationJsonSerializer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeviceConfigurationJsonDeserializer implements JsonDeserializer<DeviceConfiguration> {
    public static DeviceConfigurationJsonDeserializer create() {
        return new DeviceConfigurationJsonDeserializer(RegattaConfigurationJsonDeserializer.create());
    }

    private final JsonDeserializer<RegattaConfiguration> regattaConfigurationDeserializer;

    public DeviceConfigurationJsonDeserializer(JsonDeserializer<RegattaConfiguration> regattaConfigurationDeserializer) {
        this.regattaConfigurationDeserializer = regattaConfigurationDeserializer;
    }

    @Override
    public DeviceConfiguration deserialize(JSONObject object) throws JsonDeserializationException {
        RegattaConfiguration proceduresConfiguration = null;
        final String name = (String) object.get(DeviceConfigurationJsonSerializer.FIELD_NAME);
        final String idAsString = (String) object.get(DeviceConfigurationJsonSerializer.FIELD_ID_AS_STRING);
        final UUID uuid = idAsString == null ? null : UUID.fromString(idAsString);
        if (object.containsKey(DeviceConfigurationJsonSerializer.FIELD_REGATTA_CONFIGURATION)) {
            JSONObject proceduresObject = Helpers.getNestedObjectSafe(object,
                    DeviceConfigurationJsonSerializer.FIELD_REGATTA_CONFIGURATION);
            proceduresConfiguration = regattaConfigurationDeserializer.deserialize(proceduresObject);
        }
        DeviceConfigurationImpl configuration = createConfiguration(proceduresConfiguration, uuid, name);
        if (object.containsKey(DeviceConfigurationJsonSerializer.FIELD_COURSE_AREA_NAMES)) {
            JSONArray courseAreaNames = Helpers.getNestedArraySafe(object,
                    DeviceConfigurationJsonSerializer.FIELD_COURSE_AREA_NAMES);
            List<String> allowedCourseAreaNames = new ArrayList<String>();
            for (Object courseAreaName : courseAreaNames) {
                allowedCourseAreaNames.add(courseAreaName.toString());
            }
            configuration.setAllowedCourseAreaNames(allowedCourseAreaNames);
        }
        if (object.containsKey(DeviceConfigurationJsonSerializer.FIELD_RESULTS_RECIPIENT)) {
            String resultsRecipient = (String) object.get(DeviceConfigurationJsonSerializer.FIELD_RESULTS_RECIPIENT);
            configuration.setResultsMailRecipient(resultsRecipient);
        }
        if (object.containsKey(DeviceConfigurationJsonSerializer.FIELD_BY_VALUE_COURSE_DESIGNER_COURSE_NAMES)) {
            JSONArray namesArray = Helpers.getNestedArraySafe(object,
                    DeviceConfigurationJsonSerializer.FIELD_BY_VALUE_COURSE_DESIGNER_COURSE_NAMES);
            List<String> names = new ArrayList<String>();
            for (Object courseName : namesArray) {
                names.add(courseName.toString());
            }
            configuration.setByNameDesignerCourseNames(names);
        }
        return configuration;
    }

    protected DeviceConfigurationImpl createConfiguration(RegattaConfiguration proceduresConfiguration, UUID uuid, String name) {
        return new DeviceConfigurationImpl(proceduresConfiguration, uuid, name);
    }
}

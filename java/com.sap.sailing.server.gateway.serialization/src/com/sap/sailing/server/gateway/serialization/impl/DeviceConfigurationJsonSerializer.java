package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.RegattaConfiguration;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class DeviceConfigurationJsonSerializer implements JsonSerializer<DeviceConfiguration> {

    public static final String FIELD_COURSE_AREA_NAMES = "courseAreaNames";
    public static final String FIELD_RESULTS_RECIPIENT = "resultsRecipient";
    public static final Object FIELD_DEFAULT_RACING_PROCEDURE_TYPE = "defaultRacingProcedureType";
    public static final Object FIELD_DEFAULT_COURSE_DESIGNER_MODE = "defaultCourseDesignerMode";
    public static final String FIELD_BY_VALUE_COURSE_DESIGNER_COURSE_NAMES = "byValueCourseNames";
    public static final String FIELD_REGATTA_CONFIGURATION = "procedures";
    
    public static DeviceConfigurationJsonSerializer create() {
        return new DeviceConfigurationJsonSerializer(RegattaConfigurationJsonSerializer.create());
    }
    
    private final JsonSerializer<RegattaConfiguration> regattaConfigurationSerializer;
    
    public DeviceConfigurationJsonSerializer(JsonSerializer<RegattaConfiguration> serializer) {
        this.regattaConfigurationSerializer = serializer;
    }

    @Override
    public JSONObject serialize(DeviceConfiguration object) {
        JSONObject result = new JSONObject();
        
        if (object.getRegattaConfiguration() != null) {
            result.put(FIELD_REGATTA_CONFIGURATION, 
                    regattaConfigurationSerializer.serialize(object.getRegattaConfiguration()));
        }

        if (object.getAllowedCourseAreaNames() != null) {
            JSONArray courseAreaNames = new JSONArray();
            courseAreaNames.addAll(object.getAllowedCourseAreaNames());
            result.put(FIELD_COURSE_AREA_NAMES, courseAreaNames);
        }
        
        if (object.getResultsMailRecipient() != null) {
            result.put(FIELD_RESULTS_RECIPIENT, object.getResultsMailRecipient());
        }
        
        if (object.getDefaultRacingProcedureType() != null) {
            result.put(FIELD_DEFAULT_RACING_PROCEDURE_TYPE, object.getDefaultRacingProcedureType().name());
        }
        
        if (object.getDefaultCourseDesignerMode() != null) {
            result.put(FIELD_DEFAULT_COURSE_DESIGNER_MODE, object.getDefaultCourseDesignerMode().name());
        }
        
        if (object.getByNameCourseDesignerCourseNames() != null) {
            JSONArray nameArray = new JSONArray();
            nameArray.addAll(object.getByNameCourseDesignerCourseNames());
            result.put(FIELD_BY_VALUE_COURSE_DESIGNER_COURSE_NAMES, nameArray);
        }

        return result;
    }

}

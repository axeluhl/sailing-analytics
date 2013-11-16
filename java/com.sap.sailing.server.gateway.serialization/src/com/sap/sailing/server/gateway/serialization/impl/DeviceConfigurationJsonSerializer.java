package com.sap.sailing.server.gateway.serialization.impl;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;
import com.sap.sailing.server.gateway.serialization.JsonSerializer;

public class DeviceConfigurationJsonSerializer implements JsonSerializer<DeviceConfiguration> {

    public static final String FIELD_COURSE_AREA_NAMES = "courseAreaNames";
    public static final String FIELD_RESULTS_RECIPIENT = "resultsRecipient";
    public static final String FIELD_BY_VALUE_COURSE_DESIGNER_COURSE_NAMES = "byValueCourseNames";
    public static final String FIELD_PROCEDURES_CONFIGURATION = "procedures";
    
    public static DeviceConfigurationJsonSerializer create() {
        return new DeviceConfigurationJsonSerializer(new RacingProceduresConfigurationJsonSerializer());
    }
    
    private final JsonSerializer<RacingProceduresConfiguration> proceduresSerializer;
    
    public DeviceConfigurationJsonSerializer(JsonSerializer<RacingProceduresConfiguration> proceduresSerializer) {
        this.proceduresSerializer = proceduresSerializer;
    }

    @Override
    public JSONObject serialize(DeviceConfiguration object) {
        JSONObject result = new JSONObject();
        
        result.put(FIELD_PROCEDURES_CONFIGURATION, 
                proceduresSerializer.serialize(object.getRacingProceduresConfiguration()));

        if (object.getAllowedCourseAreaNames() != null) {
            JSONArray courseAreaNames = new JSONArray();
            courseAreaNames.addAll(object.getAllowedCourseAreaNames());
            result.put(FIELD_COURSE_AREA_NAMES, courseAreaNames);
        }
        
        if (object.getResultsMailRecipient() != null) {
            result.put(FIELD_RESULTS_RECIPIENT, object.getResultsMailRecipient());
        }
        
        if (object.getByNameCourseDesignerCourseNames() != null) {
            JSONArray nameArray = new JSONArray();
            nameArray.addAll(object.getByNameCourseDesignerCourseNames());
            result.put(FIELD_BY_VALUE_COURSE_DESIGNER_COURSE_NAMES, nameArray);
        }

        return result;
    }

}

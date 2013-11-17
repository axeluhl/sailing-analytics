package com.sap.sailing.server.gateway.deserialization.impl;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sap.sailing.domain.base.configuration.DeviceConfiguration;
import com.sap.sailing.domain.base.configuration.RacingProceduresConfiguration;
import com.sap.sailing.domain.base.configuration.impl.DeviceConfigurationImpl;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;
import com.sap.sailing.server.gateway.serialization.impl.DeviceConfigurationJsonSerializer;

public class DeviceConfigurationJsonDeserializer implements JsonDeserializer<DeviceConfiguration> {
    
    public static DeviceConfigurationJsonDeserializer create() {
        return new DeviceConfigurationJsonDeserializer(new RacingProceduresConfigurationJsonDeserializer());
    }

    private final JsonDeserializer<? extends RacingProceduresConfiguration> proceduresDeserializer;
    
    public DeviceConfigurationJsonDeserializer(JsonDeserializer<? extends RacingProceduresConfiguration> proceduresDeserializer) {
        this.proceduresDeserializer = proceduresDeserializer;
    }
    
    @Override
    public DeviceConfiguration deserialize(JSONObject object) throws JsonDeserializationException {
        
        JSONObject proceduresObject = 
                Helpers.getNestedObjectSafe(object, DeviceConfigurationJsonSerializer.FIELD_PROCEDURES_CONFIGURATION);
        RacingProceduresConfiguration proceduresConfiguration = proceduresDeserializer.deserialize(proceduresObject);
        
        DeviceConfigurationImpl configuration = createConfiguration(proceduresConfiguration);

        if (object.containsKey(DeviceConfigurationJsonSerializer.FIELD_COURSE_AREA_NAMES)) {
            JSONArray courseAreaNames = Helpers.getNestedArraySafe(object, DeviceConfigurationJsonSerializer.FIELD_COURSE_AREA_NAMES);
            List<String> allowedCourseAreaNames = new ArrayList<String>();
            for (Object name : courseAreaNames) {
                allowedCourseAreaNames.add(name.toString());
            }
            configuration.setAllowedCourseAreaNames(allowedCourseAreaNames);
        }
        
        if (object.containsKey(DeviceConfigurationJsonSerializer.FIELD_DEFAULT_RACING_PROCEDURE_TYPE)) {
            RacingProcedureType type = RacingProcedureType.valueOf(
                    object.get(DeviceConfigurationJsonSerializer.FIELD_DEFAULT_RACING_PROCEDURE_TYPE).toString());
            configuration.setDefaultRacingProcedureType(type);
        }
        
        if (object.containsKey(DeviceConfigurationJsonSerializer.FIELD_DEFAULT_COURSE_DESIGNER_MODE)) {
            CourseDesignerMode mode = CourseDesignerMode.valueOf(
                    object.get(DeviceConfigurationJsonSerializer.FIELD_DEFAULT_COURSE_DESIGNER_MODE).toString());
            configuration.setDefaultCourseDesignerMode(mode);
        }

        if (object.containsKey(DeviceConfigurationJsonSerializer.FIELD_RESULTS_RECIPIENT)) {
            String resultsRecipient = (String) object.get(DeviceConfigurationJsonSerializer.FIELD_RESULTS_RECIPIENT);
            configuration.setResultsMailRecipient(resultsRecipient);
        }

        if (object.containsKey(DeviceConfigurationJsonSerializer.FIELD_BY_VALUE_COURSE_DESIGNER_COURSE_NAMES)) {
            JSONArray namesArray = Helpers.getNestedArraySafe(object, 
                    DeviceConfigurationJsonSerializer.FIELD_BY_VALUE_COURSE_DESIGNER_COURSE_NAMES);
            
            List<String> names = new ArrayList<String>();
            for (Object name : namesArray) {
                names.add(name.toString());
            }
            configuration.setByNameDesignerCourseNames(names);
        }

        return configuration;
    }
    
    protected DeviceConfigurationImpl createConfiguration(RacingProceduresConfiguration proceduresConfiguration) {
        return new DeviceConfigurationImpl(proceduresConfiguration);
    }

}

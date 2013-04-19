package com.sap.sailing.domain.tractracadapter.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.tractracadapter.CourseUpdateResponse;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;

public class CourseUpdateResponseDeserializer implements JsonDeserializer<CourseUpdateResponse> {
    
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_MESSAGE = "message";

    @Override
    public CourseUpdateResponse deserialize(JSONObject object) throws JsonDeserializationException {
        String status = (String) object.get(FIELD_STATUS);
        String message = (String) object.get(FIELD_MESSAGE);
        
        CourseUpdateResponse response = new CourseUpdateResponseImpl(status, message);
        return response;
    }

}

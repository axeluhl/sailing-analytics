package com.sap.sailing.domain.tractracadapter.impl;

import org.json.simple.JSONObject;

import com.sap.sailing.domain.tractracadapter.UpdateResponse;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializationException;
import com.sap.sailing.server.gateway.deserialization.JsonDeserializer;

public class UpdateResponseDeserializer implements JsonDeserializer<UpdateResponse> {
    
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_MESSAGE = "message";

    @Override
    public UpdateResponse deserialize(JSONObject object) throws JsonDeserializationException {
        String status = (String) object.get(FIELD_STATUS);
        String message = (String) object.get(FIELD_MESSAGE);
        
        UpdateResponse response = new UpdateResponseImpl(status, message);
        return response;
    }

}

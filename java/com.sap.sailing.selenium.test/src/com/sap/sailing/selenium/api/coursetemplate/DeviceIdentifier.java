package com.sap.sailing.selenium.api.coursetemplate;

import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.JsonWrapper;

public class DeviceIdentifier extends JsonWrapper {
    private static final String FIELD_DEVICE_ID = "id";
    private static final String FIELD_DEVICE_TYPE = "type";
    private static final String FIELD_STRING_REPRESENTATION = "stringRepresentation";

    public DeviceIdentifier(JSONObject json) {
        super(json);
    }
    
    public String getId() {
        return get(FIELD_DEVICE_ID);
    }

    public String getType() {
        return get(FIELD_DEVICE_TYPE);
    }
    
    public String getStringRepresentation() {
        return get(FIELD_STRING_REPRESENTATION);
    }
}

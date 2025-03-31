package com.sap.sailing.selenium.api.event;

import java.util.UUID;

import org.json.simple.JSONObject;

import com.sap.sailing.selenium.api.core.JsonWrapper;

public class Mark extends JsonWrapper {
    
    private static final String ATTRIBUTE_MARK_ID = "markId";
    public static final String FIELD_ORIGINATING_MARK_PROPERTIES_ID = "originatingMarkPropertiesId";

    public Mark(JSONObject json) {
        super(json);
    }

    public final UUID getMarkId() {
        return UUID.fromString(get(ATTRIBUTE_MARK_ID));
    }
    
    public final UUID getOriginatingMarkPropertiesId() {
        String stringValue = get(FIELD_ORIGINATING_MARK_PROPERTIES_ID);
        return stringValue == null ? null : UUID.fromString(stringValue);
    }
}
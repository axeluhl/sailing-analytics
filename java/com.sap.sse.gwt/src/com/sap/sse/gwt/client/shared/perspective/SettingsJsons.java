package com.sap.sse.gwt.client.shared.perspective;

import com.google.gwt.json.client.JSONObject;

public class SettingsJsons {
    
    private final JSONObject globalSettingsJson;
    private final JSONObject contextSpecificSettingsJson;
    
    public SettingsJsons(JSONObject globalSettingsJson, JSONObject contextSpecificSettingsJson) {
        this.globalSettingsJson = globalSettingsJson;
        this.contextSpecificSettingsJson = contextSpecificSettingsJson;
    }
    
    public JSONObject getGlobalSettingsJson() {
        return globalSettingsJson;
    }
    
    public JSONObject getContextSpecificSettingsJson() {
        return contextSpecificSettingsJson;
    }
    
}

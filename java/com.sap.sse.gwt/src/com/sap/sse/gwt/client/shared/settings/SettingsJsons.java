package com.sap.sse.gwt.client.shared.settings;

import com.google.gwt.json.client.JSONObject;

/**
 * Simple class which wraps the JSON representations User Settings and Document Settings
 * as two {@link JSONObject}s.
 * 
 * @author Vladislav Chumak
 *
 */
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

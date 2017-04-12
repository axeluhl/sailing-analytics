package com.sap.sse.security.ui.settings;

import com.google.gwt.json.client.JSONValue;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.perspective.SettingsJsons;

public class UserSettingsBuildingPipeline extends AbstractSettingsBuildingPipeline {
    
    
    public UserSettingsBuildingPipeline(SettingsStringConverter settingsStringConverter) {
        super(settingsStringConverter);
    }
    
    public UserSettingsBuildingPipeline() {
    }
    
    public<S extends Settings> S getSettingsObject(S defaultSettings, SettingsJsons settingsJsons) {
        if(settingsJsons.getContextSpecificSettingsJson() != null) {
            defaultSettings = settingsStringConverter.deserializeFromJson(defaultSettings, settingsJsons.getContextSpecificSettingsJson());
        } else if(settingsJsons.getGlobalSettingsJson() != null) {
            defaultSettings = settingsStringConverter.deserializeFromJson(defaultSettings, settingsJsons.getGlobalSettingsJson());
        }
        defaultSettings = settingsStringConverter.deserializeFromCurrentUrl(defaultSettings);
        return defaultSettings;
    }

    @Override
    public JSONValue getJsonObject(Settings newSettings) {
        return settingsStringConverter.serializeFromSettingsObject(newSettings);
    }
    
}

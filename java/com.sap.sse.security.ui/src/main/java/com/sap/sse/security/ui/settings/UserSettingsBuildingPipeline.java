package com.sap.sse.security.ui.settings;

import java.util.List;

import com.google.gwt.json.client.JSONValue;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.settings.AbstractSettingsBuildingPipeline;
import com.sap.sse.gwt.client.shared.settings.PipelineLevel;
import com.sap.sse.gwt.client.shared.settings.SettingsJsons;
import com.sap.sse.gwt.client.shared.settings.SettingsStringConverter;

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
    public JSONValue getJsonObject(Settings newSettings, PipelineLevel pipelineLevel, List<String> path) {
        return settingsStringConverter.serializeFromSettingsObject(newSettings);
    }
    
}

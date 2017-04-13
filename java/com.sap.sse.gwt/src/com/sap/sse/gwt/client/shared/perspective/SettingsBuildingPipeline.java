package com.sap.sse.gwt.client.shared.perspective;

import java.util.List;

import com.google.gwt.json.client.JSONValue;
import com.sap.sse.common.settings.Settings;

public interface SettingsBuildingPipeline {

    <S extends Settings> S getSettingsObject(S defaultSettings, SettingsJsons settingsJsons);
    
    <S extends Settings> S getSettingsObject(S defaultSettings);

    <S extends Settings> S getSettingsObject(S defaultSettings, SettingsStrings settingsStrings);
    
    JSONValue getJsonObject(Settings settings, PipelineLevel pipelineLevel, List<String> path);

    SettingsStringConverter getSettingsStringConverter();

}

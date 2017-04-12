package com.sap.sse.security.ui.settings;

import com.google.gwt.json.client.JSONValue;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.perspective.SettingsJsons;
import com.sap.sse.gwt.client.shared.perspective.SettingsStrings;

public interface SettingsBuildingPipeline {

    <S extends Settings> S getSettingsObject(S defaultSettings, SettingsJsons settingsJsons);
    
    <S extends Settings> S getSettingsObject(S defaultSettings);

    <S extends Settings> S getSettingsObject(S defaultSettings, SettingsStrings settingsStrings);
    
    JSONValue getJsonObject(Settings settings);

    SettingsStringConverter getSettingsStringConverter();

}

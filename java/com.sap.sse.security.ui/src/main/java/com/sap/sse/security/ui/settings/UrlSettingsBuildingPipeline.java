package com.sap.sse.security.ui.settings;

import com.google.gwt.json.client.JSONValue;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.perspective.SettingsJsons;

public class UrlSettingsBuildingPipeline extends AbstractSettingsBuildingPipeline {
    
    @Override
    public <S extends Settings> S getSettingsObject(S defaultSettings, SettingsJsons settingsJsons) {
        return settingsStringConverter.deserializeFromCurrentUrl(defaultSettings);
    }

    @Override
    public JSONValue getJsonObject(Settings settings) {
        throw new UnsupportedOperationException("This pipeline does not support JSON conversion");
    }
    
}

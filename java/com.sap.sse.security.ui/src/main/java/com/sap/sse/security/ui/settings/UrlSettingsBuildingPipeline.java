package com.sap.sse.security.ui.settings;

import java.util.List;

import com.google.gwt.json.client.JSONValue;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.settings.AbstractSettingsBuildingPipeline;
import com.sap.sse.gwt.client.shared.settings.PipelineLevel;
import com.sap.sse.gwt.client.shared.settings.SettingsJsons;

public class UrlSettingsBuildingPipeline extends AbstractSettingsBuildingPipeline {
    
    @Override
    public <S extends Settings> S getSettingsObject(S defaultSettings, SettingsJsons settingsJsons) {
        return settingsStringConverter.deserializeFromCurrentUrl(defaultSettings);
    }

    @Override
    public JSONValue getJsonObject(Settings settings, PipelineLevel pipelineLevel, List<String> path) {
        throw new UnsupportedOperationException("This pipeline does not support JSON conversion");
    }
    
}

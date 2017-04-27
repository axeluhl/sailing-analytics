package com.sap.sse.security.ui.settings;

import java.util.List;

import com.google.gwt.json.client.JSONValue;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.settings.AbstractSettingsBuildingPipeline;
import com.sap.sse.gwt.client.shared.settings.PipelineLevel;
import com.sap.sse.gwt.client.shared.settings.SettingsJsons;
import com.sap.sse.gwt.client.shared.settings.SettingsStorageManager;

/**
 * Settings building pipeline which is only capable of reading settings from URL.
 * Conversion to JSON is not supported. This implementation is supposed to be used by {@link SettingsStorageManager}
 * which offers read-only functionality for settings, and thus, does not provide persistence support.
 * 
 * 
 * @author Vladislav Chumak
 *
 */
public class UrlSettingsBuildingPipeline extends AbstractSettingsBuildingPipeline {
    
    /**
     * Constructs a settings object by means of provided defaultSettings and current URL.
     * 
     * @param defaultSettings The basic settings to be used
     * @param settingsJsons The persisted representation of User Settings and Document Settings, which is ignored by this implementation
     * @return The constructed settings object
     */
    @Override
    public <S extends Settings> S getSettingsObject(S defaultSettings, SettingsJsons settingsJsons) {
        return settingsStringConverter.deserializeFromCurrentUrl(defaultSettings);
    }

    /**
     * This implementation does not provide support for JSON conversion of settings objects, because it is supposed to be used
     * by read-only {@link SettingsStorageManager} implementations.
     */
    @Override
    public JSONValue getJsonObject(Settings settings, PipelineLevel pipelineLevel, List<String> path) {
        throw new UnsupportedOperationException("This pipeline does not support JSON conversion");
    }
    
}

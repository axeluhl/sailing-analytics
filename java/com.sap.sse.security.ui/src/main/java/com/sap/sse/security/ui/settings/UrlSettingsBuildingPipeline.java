package com.sap.sse.security.ui.settings;

import java.util.List;

import com.google.gwt.json.client.JSONValue;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.settings.PipelineLevel;
import com.sap.sse.gwt.client.shared.settings.SettingsBuildingPipeline;
import com.sap.sse.gwt.client.shared.settings.SettingsJsons;
import com.sap.sse.gwt.client.shared.settings.SettingsSerializationHelper;
import com.sap.sse.gwt.client.shared.settings.SettingsStorageManager;

/**
 * Settings building pipeline which is only capable of reading settings from URL. Conversion to JSON is not supported.
 * This implementation is supposed to be used by {@link SettingsStorageManager} which offers read-only functionality for
 * settings, and thus, does not provide persistence support.
 * 
 * 
 * @author Vladislav Chumak
 *
 */
public class UrlSettingsBuildingPipeline implements SettingsBuildingPipeline<SettingsJsons> {

    /**
     * Conversion helper which is used by this instance for type conversion/serialization between settings objects and
     * JSON Strings.
     */
    protected final SettingsSerializationHelper settingsSerializationHelper;

    /**
     * Constructs an instance with a custom conversion helper between settings objects and its JSON representation.
     * 
     * @param settingsSerializationHelper
     *            The custom conversion helper
     */
    public UrlSettingsBuildingPipeline(SettingsSerializationHelper settingsSerializationHelper) {
        this.settingsSerializationHelper = settingsSerializationHelper;
    }

    /**
     * Constructs a settings object by means of provided defaultSettings and current URL.
     * 
     * @param defaultSettings
     *            The basic settings to be used
     * @param settingsRepresentation
     *            The persisted representation of Settings, which is ignored by this implementation
     * @return The constructed settings object
     */
    @Override
    public <S extends Settings> S getSettingsObject(S defaultSettings, SettingsJsons settingsRepresentation) {
        return settingsSerializationHelper.deserializeFromCurrentUrl(defaultSettings);
    }

    /**
     * This implementation does not provide support for JSON conversion of settings objects, because it is supposed to
     * be used by read-only {@link SettingsStorageManager} implementations.
     */
    @Override
    public JSONValue getJsonObject(Settings settings, PipelineLevel pipelineLevel, List<String> path) {
        throw new UnsupportedOperationException("This pipeline does not support JSON conversion");
    }

}

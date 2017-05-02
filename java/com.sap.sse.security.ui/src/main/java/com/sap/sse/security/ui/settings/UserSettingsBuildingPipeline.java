package com.sap.sse.security.ui.settings;

import java.util.List;

import com.google.gwt.json.client.JSONValue;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.settings.AbstractSettingsBuildingPipeline;
import com.sap.sse.gwt.client.shared.settings.PipelineLevel;
import com.sap.sse.gwt.client.shared.settings.SettingsJsons;
import com.sap.sse.gwt.client.shared.settings.SettingsStringConverter;

/**
 * Settings building pipeline which is capable of building settings considering
 * System Default Settings, persisted representations of
 * User Settings and Document Settings, and current URL.
 * The precedence of settings is:
 * <ul>
 *  <li>System Default Settings</li>
 *  <li>User Settings</li>
 *  <li>Document Settings</li>
 *  <li>URL Settings</li>
 * </ul>
 * 
 * @author Vladislav Chumak
 *
 */
public class UserSettingsBuildingPipeline extends AbstractSettingsBuildingPipeline {
    
    /**
     * Constructs an instance with a custom conversion helper between
     * settings objects and its JSON representation.
     */
    protected UserSettingsBuildingPipeline(SettingsStringConverter settingsStringConverter) {
        super(settingsStringConverter);
    }
    
    /**
     * Constructs an instance with {@link SettingsStringConverter} as conversion helper between
     * settings objects and its JSON representation.
     */
    public UserSettingsBuildingPipeline() {
    }
    
    /**
     * Constructs a settings object by means of provided defaultSettings, persisted representations of
     * User Settings and Document Settings, and current URL.
     * 
     * @param defaultSettings The basic settings to be used
     * @param settingsJsons The persisted representation of User Settings and Document Settings
     * @return The constructed settings object
     */
    public<S extends Settings> S getSettingsObject(S defaultSettings, SettingsJsons settingsJsons) {
        if(settingsJsons.getContextSpecificSettingsJson() != null) {
            defaultSettings = settingsStringConverter.deserializeFromJson(defaultSettings, settingsJsons.getContextSpecificSettingsJson());
        } else if(settingsJsons.getGlobalSettingsJson() != null) {
            defaultSettings = settingsStringConverter.deserializeFromJson(defaultSettings, settingsJsons.getGlobalSettingsJson());
        }
        defaultSettings = settingsStringConverter.deserializeFromCurrentUrl(defaultSettings);
        return defaultSettings;
    }

    /**
     * Converts the provided settings object into a JSON representation without considering provided pipeline level and settings tree path.
     * 
     * @param settings The settings to convert to JSON representation
     * @param pipelineLevel The pipeline level which indicates the storage scope, e.g. User Settings or Document Settings.
     * @param path The path of the settings in the settings tree
     * @return The JSON representation of the provided settings
     */
    @Override
    public JSONValue getJsonObject(Settings newSettings, PipelineLevel pipelineLevel, List<String> path) {
        return settingsStringConverter.serializeFromSettingsObject(newSettings);
    }
    
}

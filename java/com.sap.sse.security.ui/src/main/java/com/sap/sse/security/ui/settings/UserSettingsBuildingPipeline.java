package com.sap.sse.security.ui.settings;

import java.util.List;

import com.google.gwt.json.client.JSONValue;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.perspective.IgnoreLocalSettings;
import com.sap.sse.gwt.client.shared.settings.PipelineLevel;
import com.sap.sse.gwt.client.shared.settings.SettingsJsons;
import com.sap.sse.gwt.client.shared.settings.SettingsSerializationHelper;

/**
 * Settings building pipeline which is capable of building settings considering System Default Settings, persisted
 * representations of User Settings and Document Settings, and current URL. The precedence of settings is:
 * <ul>
 * <li>System Default Settings</li>
 * <li>User Settings</li>
 * <li>Document Settings</li>
 * <li>URL Settings</li>
 * </ul>
 * 
 * @author Vladislav Chumak
 *
 */
public class UserSettingsBuildingPipeline extends UrlSettingsBuildingPipeline {

    /**
     * Constructs an instance with a custom conversion helper between settings objects and its JSON representation.
     * 
     * @param settingsSerializationHelper
     *            The custom conversion helper
     */
    public UserSettingsBuildingPipeline(SettingsSerializationHelper settingsStringConverter) {
        super(settingsStringConverter);
    }

    /**
     * Constructs a settings object by means of provided defaultSettings, persisted representations of User Settings and
     * Document Settings, and current URL.
     * 
     * @param defaultSettings
     *            The basic settings to be used
     * @param settingsJsons
     *            The persisted representation of User Settings and Document Settings
     * @return The constructed settings object
     */
    @Override
    public <CS extends Settings> CS getSettingsObject(CS defaultSettings, SettingsJsons settingsJsons) {
        if (!isIgnoreLocalSettingsUrlFlagPresent()) {
            if (settingsJsons.getContextSpecificSettingsJson() != null) {
                defaultSettings = settingsSerializationHelper.deserializeFromJson(defaultSettings,
                        settingsJsons.getContextSpecificSettingsJson());
            } else if (settingsJsons.getGlobalSettingsJson() != null) {
                defaultSettings = settingsSerializationHelper.deserializeFromJson(defaultSettings,
                        settingsJsons.getGlobalSettingsJson());
            }
        }
        defaultSettings = settingsSerializationHelper.deserializeFromCurrentUrl(defaultSettings);
        return defaultSettings;
    }

    /**
     * Converts the provided settings object into a JSON representation without considering provided pipeline level and
     * settings tree path.
     * 
     * @param settings
     *            The settings to convert to JSON representation
     * @param pipelineLevel
     *            The pipeline level which indicates the storage scope, e.g. User Settings or Document Settings.
     * @param path
     *            The path of the settings in the settings tree
     * @return The JSON representation of the provided settings
     */
    @Override
    public JSONValue getJsonObject(Settings newSettings, PipelineLevel pipelineLevel, List<String> path) {
        return settingsSerializationHelper.serializeFromSettingsObject(newSettings);
    }

    public boolean isIgnoreLocalSettingsUrlFlagPresent() {
        return settingsSerializationHelper.deserializeFromCurrentUrl(new IgnoreLocalSettings()).isIgnoreLocalSettings();
    }

}

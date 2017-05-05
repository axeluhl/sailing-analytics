package com.sap.sse.security.ui.settings;

import java.util.List;

import com.google.gwt.json.client.JSONObject;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.settings.PersistableSettingsRepresentations;
import com.sap.sse.gwt.client.shared.settings.PipelineLevel;
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
     * @param settingsRepresentations
     *            The persisted representation of User Settings and Document Settings
     * @return The constructed settings object
     */
    @Override
    public <CS extends Settings> CS getSettingsObject(CS defaultSettings,
            PersistableSettingsRepresentations<JSONObject> settingsRepresentations) {
        if (settingsRepresentations.getContextSpecificSettingsRepresentation() != null) {
            defaultSettings = settingsSerializationHelper.deserializeFromJson(defaultSettings,
                    settingsRepresentations.getContextSpecificSettingsRepresentation());
        } else if (settingsRepresentations.getGlobalSettingsRepresentation() != null) {
            defaultSettings = settingsSerializationHelper.deserializeFromJson(defaultSettings,
                    settingsRepresentations.getGlobalSettingsRepresentation());
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
    public JSONObject getPersistableSettingsRepresentation(Settings newSettings, PipelineLevel pipelineLevel,
            List<String> path) {
        return settingsSerializationHelper.serializeFromSettingsObject(newSettings);
    }

}

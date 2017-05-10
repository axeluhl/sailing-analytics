package com.sap.sse.security.ui.settings;

import java.util.List;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.settings.PipelineLevel;
import com.sap.sse.gwt.client.shared.settings.SettingsRepresentationTransformer;
import com.sap.sse.gwt.client.shared.settings.StorableRepresentationOfDocumentAndUserSettings;
import com.sap.sse.gwt.client.shared.settings.StorableSettingsRepresentation;

/**
 * Settings building pipeline which is capable of building settings considering System Default Settings, stored
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
    public UserSettingsBuildingPipeline(SettingsRepresentationTransformer settingsStringConverter) {
        super(settingsStringConverter);
    }

    /**
     * Constructs a settings object by means of provided {@code systemDefaultSettings}, stored representations of User
     * Settings and Document Settings, and current URL.
     * 
     * @param systemDefaultSettings
     *            The basic settings to be used
     * @param settingsRepresentation
     *            The stored representation of User Settings and Document Settings
     * @return The constructed settings object
     */
    @Override
    public <CS extends Settings> CS getSettingsObject(CS systemDefaultSettings,
            StorableRepresentationOfDocumentAndUserSettings settingsRepresentation) {
        CS effectiveSettings = systemDefaultSettings;
        if (settingsRepresentation.hasStoredDocumentSettings()) {
            effectiveSettings = settingsSerializationHelper.mergeSettingsObjectWithStorableRepresentation(effectiveSettings,
                    settingsRepresentation.getDocumentSettingsRepresentation());
        } else if (settingsRepresentation.hasStoredUserSettings()) {
            effectiveSettings = settingsSerializationHelper.mergeSettingsObjectWithStorableRepresentation(effectiveSettings,
                    settingsRepresentation.getUserSettingsRepresentation());
        }
        effectiveSettings = settingsSerializationHelper.mergeSettingsObjectWithUrlSettings(effectiveSettings);
        return effectiveSettings;
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
     * @return The storable settings representation of the provided settings
     */
    @Override
    public StorableSettingsRepresentation getStorableSettingsRepresentation(Settings newSettings,
            PipelineLevel pipelineLevel, List<String> path) {
        return settingsSerializationHelper.convertToSettingsRepresentation(newSettings);
    }

}

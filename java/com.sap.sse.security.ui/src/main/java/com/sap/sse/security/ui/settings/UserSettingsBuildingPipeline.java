package com.sap.sse.security.ui.settings;

import java.util.List;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.support.SettingsUtil;
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
     * Constructs an instance with a custom conversion helper between settings objects and its storable representation.
     * 
     * @param settingsRepresentationTransformer
     *            The custom conversion helper
     */
    public UserSettingsBuildingPipeline(SettingsRepresentationTransformer settingsRepresentationTransformer) {
        super(settingsRepresentationTransformer);
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
        if (settingsRepresentation.hasStoredUserSettings()) {
            effectiveSettings = settingsRepresentationTransformer.mergeSettingsObjectWithStorableRepresentation(effectiveSettings,
                    settingsRepresentation.getUserSettingsRepresentation());
        }
        if (settingsRepresentation.hasStoredDocumentSettings()) {
            effectiveSettings = settingsRepresentationTransformer.mergeSettingsObjectWithStorableRepresentation(effectiveSettings,
                    settingsRepresentation.getDocumentSettingsRepresentation());
        }
        effectiveSettings = settingsRepresentationTransformer.mergeSettingsObjectWithUrlSettings(effectiveSettings);
        return effectiveSettings;
    }

    /**
     * Converts the provided settings object into a storable settings representation without considering provided pipeline level and
     * settings tree path.
     * 
     * @param settings
     *            The settings to convert to storable settings representation
     * @param pipelineLevel
     *            The pipeline level which indicates the storage scope, e.g. User Settings or Document Settings.
     * @param path
     *            The path of the settings in the settings tree
     * @return The storable settings representation of the provided settings
     */
    @Override
    public <CS extends Settings> StorableSettingsRepresentation getStorableRepresentationOfUserSettings(CS newSettings, CS newInstance, StorableRepresentationOfDocumentAndUserSettings previousSettingsRepresentation, List<String> path) {
        return settingsRepresentationTransformer.convertToSettingsRepresentation(newSettings);
    }
    
    /**
     * Converts the provided settings object into a storable settings representation without considering provided pipeline level and
     * settings tree path.
     * 
     * @param settings
     *            The settings to convert to storable settings representation
     * @param pipelineLevel
     *            The pipeline level which indicates the storage scope, e.g. User Settings or Document Settings.
     * @param path
     *            The path of the settings in the settings tree
     * @return The storable settings representation of the provided settings
     */
    @Override
    public <CS extends Settings> StorableSettingsRepresentation getStorableRepresentationOfDocumentSettings(CS newSettings, CS newInstance, StorableRepresentationOfDocumentAndUserSettings previousSettingsRepresentation, List<String> path) {
        CS documentSettingsWithUserSettingsDiff;
        if(previousSettingsRepresentation.hasStoredUserSettings()) {
            CS pipelinedSettings = SettingsUtil.copyDefaultsFromValues(newInstance, newInstance);
            pipelinedSettings = SettingsUtil.copyDefaults(newSettings, newInstance); //overrides values which are set to default values
            CS previousUserSettings = settingsRepresentationTransformer.mergeSettingsObjectWithStorableRepresentation(newInstance, previousSettingsRepresentation.getUserSettingsRepresentation().getSubSettingsRepresentation(path));
            pipelinedSettings = SettingsUtil.copyDefaultsFromValues(previousUserSettings, pipelinedSettings);
            documentSettingsWithUserSettingsDiff = SettingsUtil.copyValues(newSettings, pipelinedSettings);
        } else {
            documentSettingsWithUserSettingsDiff = newSettings;
        }
        return settingsRepresentationTransformer.convertToSettingsRepresentation(documentSettingsWithUserSettingsDiff);
    }
    
    

}

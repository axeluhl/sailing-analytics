package com.sap.sse.gwt.client.shared.settings;

import java.util.List;

import com.sap.sse.common.settings.Settings;

/**
 * Defines the settings construction process. The construction of the settings may be influenced by various conditions,
 * for example URL, UI, additional settings scopes and etc. It is up to the implementation of this interface to define
 * the desired way of how the settings are constructed from its stored representation considering stored Document and
 * User Settings, and how the settings are transformed back into its stored representation. The pipeline may be used in
 * {@link ComponentContext} when it loads and stores settings.
 * 
 * @author Vladislav Chumak
 * 
 *
 */
public interface SettingsBuildingPipeline {

    /**
     * Constructs a settings object by means of provided {@code systemDefaultSettings} and stored representations of Settings. This
     * method implements the settings construction pipeline for a settings object which is used for settings loading
     * operations.
     * 
     * @param systemDefaultSettings
     *            The basic settings to be used
     * @param settingsRepresentation
     *            The stored representations of User Settings and Document Settings
     * @return The constructed settings object
     */
    <CS extends Settings> CS getSettingsObject(CS systemDefaultSettings,
            StorableRepresentationOfDocumentAndUserSettings settingsRepresentation, List<String> absolutePathOfComponentWithSettings);

    /**
     * Converts the provided settings according to the storage scope and the settings tree path of provided settings.
     * This method implements the storable settings representation building pipeline which is used for settings storing
     * operations.
     * 
     * @param settings
     *            The settings to convert to storable representation
     * @param pipelineLevel
     *            The pipeline level which indicates the storage scope, e.g. User Settings or Document Settings.
     * @param path
     *            The settings tree path of provided settings
     * @return The storable settings representation of provided settings
     */
    public <CS extends Settings> StorableRepresentationOfDocumentAndUserSettings getStorableSettingsRepresentation(CS newSettings, CS systemDefaultSettings, StorableRepresentationOfDocumentAndUserSettings previousSettingsRepresentation, List<String> path);

}

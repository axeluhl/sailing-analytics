package com.sap.sse.gwt.client.shared.settings;

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
     * Constructs the settings object of the root perspective/component by means of provided
     * {@code systemDefaultSettings} and stored representation of all settings. This method implements the settings
     * construction pipeline for a settings object which is used for settings loading operations.
     * 
     * @param systemDefaultSettings
     *            The basic settings to be used
     * @param settingsRepresentation
     *            The stored representations of User Settings and Document Settings
     * @return The constructed settings object
     */
    <CS extends Settings> CS getSettingsObject(CS systemDefaultSettings,
            StorableRepresentationOfDocumentAndUserSettings settingsRepresentation);

    /**
     * Converts the provided settings according to storable settings representation for User Settings. This method
     * implements the storable settings representation building pipeline which is used for settings storing operations.
     * 
     * @param newSettings
     *            The settings to convert to storable representation
     * @param newInstance
     *            A fresh dummy instance of the settings type which will be used as temporary helper (defaultValues and
     *            values of the instance are completely ignored)
     * @param previousSettingsRepresentation
     *            The representation of settings which have been already stored (the whole settings tree)
     * @param path
     *            The settings tree path of provided settings (empty lists means the provided settings belong to the
     *            root component/perspective)
     * @return The storable settings representation of provided settings as User Settings
     */
    <CS extends Settings> StorableSettingsRepresentation getStorableRepresentationOfUserSettings(CS newSettings,
            CS newInstance, Iterable<String> path);

    /**
     * Converts the provided settings according to storable settings representation for Document Settings. This method
     * implements the storable settings representation building pipeline which is used for settings storing operations.
     * 
     * @param newSettings
     *            The settings to convert to storable representation
     * @param newInstance
     *            A fresh dummy instance of the settings type which will be used as temporary helper (defaultValues and
     *            values of the instance are completely ignored)
     * @param previousSettingsRepresentation
     *            The representation of settings which have been already stored (the whole settings tree)
     * @param path
     *            The settings tree path of provided settings (empty lists means the provided settings belong to the
     *            root component/perspective)
     * @return The storable settings representation of provided settings as Document Settings
     */
    <CS extends Settings> StorableSettingsRepresentation getStorableRepresentationOfDocumentSettings(CS newSettings,
            CS newInstance, StorableRepresentationOfDocumentAndUserSettings previousSettingsRepresentation,
            Iterable<String> path);

}

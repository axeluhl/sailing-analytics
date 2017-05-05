package com.sap.sse.gwt.client.shared.settings;

import java.util.List;

import com.sap.sse.common.settings.Settings;

/**
 * Defines the settings construction process. The construction of the settings may be influenced by various conditions,
 * for example URL, UI, additional settings scopes and etc. It is up to the implementation of this interface to define
 * the desired way of how the settings are constructed from its persistable representation considering persisted
 * Document and User Settings, and how the settings are transformed back into its persistable representation. The
 * pipeline may be used in {@link ComponentContext} when it loads and stores settings.
 * 
 * @author Vladislav Chumak
 * 
 * @param <S>
 *            The type of settings representation inside a {@link PersistableSettingsRepresentations}
 *
 */
public interface SettingsBuildingPipeline<T> {

    /**
     * Constructs a settings object by means of provided defaultSettings and persisted representations of Settings. This
     * method implements the settings construction pipeline for a settings object which is used for settings loading
     * operations.
     * 
     * @param defaultSettings
     *            The basic settings to be used
     * @param settingsRepresentations
     *            The persisted representations of User Settings and Document Settings
     * @return The constructed settings object
     */
    <CS extends Settings> CS getSettingsObject(CS defaultSettings,
            PersistableSettingsRepresentations<T> settingsRepresentations);

    /**
     * Converts the provided settings according to the storage scope and the settings tree path of provided settings.
     * This method implements the persitable settings representation building pipeline which is used for settings
     * storing operations.
     * 
     * @param settings
     *            The settings to convert to persistable representation
     * @param pipelineLevel
     *            The pipeline level which indicates the storage scope, e.g. User Settings or Document Settings.
     * @param path
     *            The settings tree path of provided settings
     * @return The persisted representation of provided settings
     */
    T getPersistableSettingsRepresentation(Settings settings, PipelineLevel pipelineLevel, List<String> path);

}

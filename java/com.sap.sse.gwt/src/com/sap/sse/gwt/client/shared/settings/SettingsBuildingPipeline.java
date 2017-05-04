package com.sap.sse.gwt.client.shared.settings;

import java.util.List;

import com.google.gwt.json.client.JSONValue;
import com.sap.sse.common.settings.Settings;

/**
 * Defines the settings construction process. The construction of the settings may be influenced
 * by various conditions, for example URL, UI, additional settings scopes and etc. It is up to the
 * implementation of this interface to define the desired way of how the settings are constructed from its persisted
 * JSON representation, and how the settings are transformed back into its persisted JSON representation.
 * The pipeline is being used in {@link SettingsStorageManager} when it loads and stores settings.
 * 
 * @author Vladislav Chumak
 * 
 * @param <S> The type of settings representation
 * @see SettingsStorageManager
 * @see AbstractSettingsBuildingPipeline
 *
 */
public interface SettingsBuildingPipeline<S> {

    /**
     * Constructs a settings object by means of provided defaultSettings and persisted representations of Settings.
     * This method implements the settings construction pipeline for a settings object which is used for settings loading operations.
     * 
     * @param defaultSettings The basic settings to be used
     * @param settingsRepresentation The persisted representation of Settings
     * @return The constructed settings object
     */
    <CS extends Settings> CS getSettingsObject(CS defaultSettings, S settingsRepresentation);
    
    /**
     * Converts the provided settings according the storage scope and path of the provided settings in the settings tree.
     * This method implements the JSON building pipeline which is used for settings storing operations.
     * 
     * @param settings The settings to convert to JSON representation
     * @param pipelineLevel The pipeline level which indicates the storage scope, e.g. User Settings or Document Settings.
     * @param path The path of the settings in the settings tree
     * @return The JSON representation of the provided settings
     */
    JSONValue getJsonObject(Settings settings, PipelineLevel pipelineLevel, List<String> path);

}

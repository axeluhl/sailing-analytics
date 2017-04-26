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
 * It is highly recommended to use at least {@link AbstractSettingsBuildingPipeline} as basic class for
 * implementation of this interface.
 * 
 * @author Vladislav Chumak
 * 
 * @see SettingsStorageManager
 * @see AbstractSettingsBuildingPipeline
 *
 */
public interface SettingsBuildingPipeline {

    /**
     * Constructs a settings object by means of provided defaultSettings by considering
     * User Settings and Document Settings.
     * This method implements the settings construction pipeline for a settings object which is used for settings loading operations.
     * 
     * @param defaultSettings The basic settings to be used
     * @param settingsJsons The persisted representation of User Settings and Document Settings
     * @return The constructed settings object
     */
    <S extends Settings> S getSettingsObject(S defaultSettings, SettingsJsons settingsJsons);
    
    /**
     * Constructs a settings object by means of provided defaultSettings by considering
     * User Settings and Document Settings.
     * This method implements the settings construction pipeline for a settings object which is used for settings loading operations.
     * 
     * @param defaultSettings The basic settings to be used
     * @param settingsJsons The persisted representation of User Settings and Document Settings
     * @return The constructed settings object
     */
    <S extends Settings> S getSettingsObject(S defaultSettings, SettingsStrings settingsStrings);
    
    /**
     * Constructs a settings object by means of provided defaultSettings without considering
     * User Settings and Document Settings.
     * This method implements the settings construction pipeline for a settings object which is used for settings loading operations.
     * 
     * @param defaultSettings The basic settings to be used
     * @return The constructed settings object
     */
    <S extends Settings> S getSettingsObject(S defaultSettings);
    
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

    /**
     * Gets the {@link SettingsStringConverter} which is used by this instance for conversion between settings objects and JSON.
     * 
     * @return The conversion helper instance used by this pipeline
     */
    SettingsStringConverter getSettingsStringConverter();

}

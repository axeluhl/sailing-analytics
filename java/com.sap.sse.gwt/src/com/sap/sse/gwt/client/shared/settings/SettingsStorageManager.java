package com.sap.sse.gwt.client.shared.settings;

import com.sap.sse.common.settings.generic.GenericSerializableSettings;

/**
 * Manages the persistence of settings. The interface was designed to support {@link ComponentContextWithSettingsStorage}.
 * The {@link SettingsStorageManager} stores values of {@link GenericSerializableSettings} which do not match the default values,
 * in other words the delta between current settings values and the default settings values.
 * The implementation may use different kind of storages, e.g. LocalStorage, and/or {@link UserService}, and etc.
 * 
 * @author Vladislav Chumak
 *
 * @param <S> The type of settings representation
 * @see ComponentContextWithSettingsStorage
 * @see GenericSerializableSettings
 */
public interface SettingsStorageManager<S> {
    
//    /**
//     * Tells whether storage of settings is supported, or not.
//     * 
//     * @return {@code True} if settings storage is supported, otherwise {@code false}
//     */
//    boolean supportsStore();

//    /**
//     * Converts the provided settings object to JSON.
//     * 
//     * @param newSettings The settings to convert
//     * @param pipelineLevel The pipeline level indicating whether the provided settings should be converted for Document Settings storage, or User Settings storage
//     * @param path The path of the corresponding settings in the settings tree according to the component which the provided settings belong to
//     * @return The converted settings as JSON
//     */
//    JSONValue settingsToJSON(Settings newSettings, PipelineLevel pipelineLevel, List<String> path);

    /**
     * Retrieves the stored JSON representations of Document Settings and User Settings which get packed together in {@link SettingsJsons}.
     * 
     * @param callback The callback which gets called when the persisted settings representation has been retrieved
     */
    void retrieveSettingsRepresentation(OnSettingsLoadedCallback<S> callback);
    
    /**
     * Stores provided settings representation.
     * 
     * @param settingsRepresentation The settings to store
     * @param onSettingsStoredCallback The callback which is called when the settings storage process gets finished
     */
    void storeSettingsRepresentation(S settingsRepresentation, OnSettingsStoredCallback onSettingsStoredCallback);
    
    /**
     * Releases all resources and listener registrations acquired by this instance.
     */
    void dispose();

}

package com.sap.sse.gwt.client.shared.settings;

import java.util.List;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;

/**
 * Manages the persistence of settings. The interface was designed to support {@link ComponentContextWithSettingsStorage}.
 * The {@link SettingsStorageManager} stores values of {@link GenericSerializableSettings} which do not match the default values,
 * in other words the delta between current settings values and the default settings values.
 * The implementation may use different kind of storages, e.g. LocalStorage, and/or {@link UserService}, and etc.
 * 
 * @author Vladislav Chumak
 *
 * @param <S> The {@link Settings} type of the settings of the root component/perspective containing all the settings for itself and its subcomponents
 * @see ComponentContextWithSettingsStorage
 * @see GenericSerializableSettings
 */
public interface SettingsStorageManager<S extends Settings> {
    
    /**
     * Tells whether the underlying implementation offers persistence for settings, or not.
     * 
     * @return {@code True} if settings storage is supported, otherwise {@code false}
     */
    boolean supportsStore();

    /**
     * Retrieves the {@link Settings} of the root component using the provided callback.
     * Depending on the storage media, the callback may be called whether synchronously
     * or asynchronously.
     * 
     * @param defaultSettings The system default settings for the root component
     * @param asyncCallback The callback to be called when the settings have been retrieved
     * from the settings storage
     */
    void retrieveDefaultSettings(S defaultSettings,
            OnSettingsLoadedCallback<S> asyncCallback);

    /**
     * Stores provided settings as User Settings (old term "Global Settings"). Only the delta between default settings and provided settings
     * will be stored.
     * 
     * @param globalSettingsJson The settings to store
     * @param onSettingsStoredCallback The callback which is called when the settings storage process gets finished
     */
    void storeGlobalSettingsJson(JSONObject globalSettingsJson, OnSettingsStoredCallback onSettingsStoredCallback);

    /**
     * Stores provided settings as Document Settings (old term "Context specific Settings"). Only the delta between default settings and provided settings
     * will be stored.
     * 
     * @param contextSpecificSettingsJson The settings to store
     * @param onSettingsStoredCallback The callback which is called when the settings storage process gets finished
     */
    void storeContextSpecificSettingsJson(JSONObject contextSpecificSettingsJson,
            OnSettingsStoredCallback onSettingsStoredCallback);

    /**
     * Converts the provided settings object to JSON.
     * 
     * @param newSettings The settings to convert
     * @param pipelineLevel The pipeline level indicating whether the provided settings should be converted for Document Settings storage, or User Settings storage
     * @param path The path of the corresponding settings in the settings tree according to the component which the provided settings belong to
     * @return The converted settings as JSON
     */
    JSONValue settingsToJSON(Settings newSettings, PipelineLevel pipelineLevel, List<String> path);

    /**
     * Retrieves the stored JSON representations of Document Settings and User Settings which get packed together in {@link SettingsJsons}.
     * 
     * @param asyncCallback The callback which gets called when the persisted settings representation has been retrieved
     */
    void retrieveSettingsJsons(AsyncCallback<SettingsJsons> asyncCallback);
    
    /**
     * Releases all resources and listener registrations acquired by this instance.
     */
    void dispose();

}

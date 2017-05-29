package com.sap.sse.gwt.client.shared.perspective;

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
 */
public interface SettingsStorageManager<S extends Settings> {
    
    boolean supportsStore();

    /**
     * Retrieves the {@link Settings} of the root component using the provided callback.
     * Depending on the storage media, the callback may be called whether synchronously
     * or anynchronously.
     * 
     * @param defaultSettings The system default settings for the root component
     * @param asyncCallback The callback to be called when the settings have been retrieved
     * from the settings storage
     */
    void retrieveDefaultSettings(S defaultSettings,
            OnSettingsLoadedCallback<S> asyncCallback);

    /**
     * Stores provided settings. Only the delta between default settings and provided settings
     * will be stored.
     * 
     * @param settingsJsons The settings to store
     * @param onSettingsStoredCallback The callback which is called when the settings storage process finishes
     * @see ComponentContextWithSettingsStorage
     * @see GenericSerializableSettings
     */
    void storeSettingsJsons(SettingsJsons settingsJsons, OnSettingsStoredCallback onSettingsStoredCallback);

    void storeGlobalSettingsJson(JSONObject globalSettingsJson, OnSettingsStoredCallback onSettingsStoredCallback);

    void storeContextSpecificSettingsJson(JSONObject contextSpecificSettingsJson,
            OnSettingsStoredCallback onSettingsStoredCallback);
    /**
     * Gets the last error occurred during settings initialisation.
     * 
     * @return The last error as {@link Throwable}, if an error occurred, otherwise {@code null}
     */
    Throwable getLastError();

    JSONValue settingsToJSON(Settings newSettings);

    void retrieveSettingsJsons(AsyncCallback<SettingsJsons> asyncCallback);
    
    void retrieveGlobalSettingsJson(AsyncCallback<JSONObject> asyncCallback);
    
    void retrieveContextSpecificSettingsJson(AsyncCallback<JSONObject> asyncCallback);
    
    void dispose();

}

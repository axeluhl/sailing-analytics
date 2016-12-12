package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.common.settings.generic.GenericSerializableSettings;

/**
 * Manages the persistence of settings. The interface was designed to support {@link AbstractComponentContextWithSettingsStorage}.
 * The {@link SettingsStorageManager} stores values of {@link GenericSerializableSettings} which do not match the default values,
 * in other words the delta between current settings values and the default settings values.
 * The implementation may use different kind of storages, e.g. LocalStorage, and/or {@link UserService}, and etc.
 * 
 * @author Vladislav Chumak
 *
 * @param <S> The {@link Settings} type of the settings of the root component/perspective containing all the settings for itself and its subcomponents
 * @see AbstractComponentContextWithSettingsStorage
 */
public interface SettingsStorageManager<S extends Settings> {

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
     * Stores provided global settings. Only the delta between default settings and provided settings
     * will be stored.
     * 
     * @param globalSettings The global settings to store
     * @see AbstractComponentContextWithSettingsStorage
     * @see GenericSerializableSettings
     */
    void storeGlobalSettings(S globalSettings);

    /**
     * Stores provided context specific settings. Only the delta between default settings and provided settings
     * will be stored.
     * 
     * @param contextSpecificSettings The context specific settings to store
     * @see AbstractComponentContextWithSettingsStorage
     * @see GenericSerializableSettings
     */
    void storeContextSpecificSettings(S contextSpecificSettings);

    /**
     * Gets the last error occurred during settings initialisation.
     * 
     * @return The last error as {@link Throwable}, if an error occurred, otherwise {@code null}
     */
    Throwable getLastError();
    
}

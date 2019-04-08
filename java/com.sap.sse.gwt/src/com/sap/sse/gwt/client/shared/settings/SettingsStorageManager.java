package com.sap.sse.gwt.client.shared.settings;

import com.sap.sse.common.settings.generic.GenericSerializableSettings;

/**
 * Manages the persistence of settings. The interface was designed to support
 * {@link ComponentContextWithSettingsStorage}. The {@link SettingsStorageManager} stores values of
 * {@link GenericSerializableSettings} which do not match the default values, in other words the delta between current
 * settings values and the default settings values. The implementation may use different kind of storages, e.g.
 * LocalStorage, and/or {@link UserService}, and etc.
 * 
 * @author Vladislav Chumak
 *
 * @see ComponentContextWithSettingsStorage
 * @see GenericSerializableSettings
 */
public interface SettingsStorageManager {

    /**
     * Retrieves the stored representations of Document Settings and User Settings which get packed together in
     * {@link StorableRepresentationOfDocumentAndUserSettings}.
     * 
     * @param callback
     *            The callback which gets called when the stored settings representations have been retrieved
     */
    void retrieveSettingsRepresentation(OnSettingsLoadedCallback<StorableRepresentationOfDocumentAndUserSettings> callback);

    /**
     * Stores provided settings representations.
     * 
     * @param settingsRepresentations
     *            The stored settings representations to store
     * @param onSettingsStoredCallback
     *            The callback which is called when the settings storage process gets finished
     */
    void storeSettingsRepresentations(StorableRepresentationOfDocumentAndUserSettings settingsRepresentations,
            OnSettingsStoredCallback onSettingsStoredCallback);

    /**
     * Releases all resources and listener registrations acquired by this instance.
     */
    void dispose();

}

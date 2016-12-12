package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;

/**
 * 
 * @author Vlad
 *
 * @param <S>
 */
public interface SettingsStorageManager<S extends Settings> {

    void retrieveDefaultSettings(S defaultSettings,
            OnSettingsLoadedCallback<S> asyncCallback);

    void storeGlobalSettings(S globalSettings);

    void storeContextSpecificSettings(S contextSpecificSettings);

    Throwable getLastError();
    
}

package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;

public interface SettingsStorageManager<S extends Settings> {

    void retrieveDefaultSettings(S defaultSettings,
            DefaultSettingsLoadedCallback<S> asyncCallback);

    void storeGlobalSettings(S globalSettings);

    void storeContextSpecificSettings(S contextSpecificSettings);

    Throwable getLastError();
    
}

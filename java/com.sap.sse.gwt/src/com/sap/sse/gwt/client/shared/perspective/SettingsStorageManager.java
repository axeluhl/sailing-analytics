package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;

public interface SettingsStorageManager<PS extends Settings> {

    void retrieveDefaultSettings(PerspectiveCompositeSettings<PS> defaultSettings,
            DefaultSettingsLoadedCallback<PS> asyncCallback);

    void storeGlobalSettings(PerspectiveCompositeSettings<PS> globalSettings);

    void storeContextSpecificSettings(PerspectiveCompositeSettings<PS> contextSpecificSettings);

    Throwable getLastError();
    
}

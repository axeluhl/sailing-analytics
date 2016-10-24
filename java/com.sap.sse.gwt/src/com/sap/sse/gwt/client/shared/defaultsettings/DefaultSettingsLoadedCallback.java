package com.sap.sse.gwt.client.shared.defaultsettings;

import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.shared.perspective.PerspectiveCompositeSettings;

public interface DefaultSettingsLoadedCallback<PS extends Settings> {
    void onError(Throwable caught, PerspectiveCompositeSettings<PS> fallbackDefaultSettings);
    void onSuccess(PerspectiveCompositeSettings<PS> defaultSettings);
}

package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;

public interface DefaultSettingsLoadedCallback<PS extends Settings> {
    void onError(Throwable caught, PerspectiveCompositeSettings<PS> fallbackDefaultSettings);
    void onSuccess(PerspectiveCompositeSettings<PS> defaultSettings);
}

package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;

public interface DefaultSettingsLoadedCallback<S extends Settings> {
    void onError(Throwable caught, S fallbackDefaultSettings);
    void onSuccess(S defaultSettings);
}

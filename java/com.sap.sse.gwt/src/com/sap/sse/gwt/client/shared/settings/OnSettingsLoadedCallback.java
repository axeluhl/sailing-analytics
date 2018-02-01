package com.sap.sse.gwt.client.shared.settings;

public interface OnSettingsLoadedCallback<S> {
    void onError(Throwable caught, S fallbackDefaultSettings);

    void onSuccess(S settings);
}

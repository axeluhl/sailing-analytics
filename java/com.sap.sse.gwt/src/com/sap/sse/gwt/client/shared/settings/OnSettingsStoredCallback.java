package com.sap.sse.gwt.client.shared.settings;

public interface OnSettingsStoredCallback {
    void onError(Throwable caught);

    void onSuccess();
}

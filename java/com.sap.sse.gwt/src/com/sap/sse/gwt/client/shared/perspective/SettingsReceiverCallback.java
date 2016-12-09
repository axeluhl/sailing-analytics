package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;

public interface SettingsReceiverCallback<S extends Settings> {
    void receiveSettings(S initialSettings);
}

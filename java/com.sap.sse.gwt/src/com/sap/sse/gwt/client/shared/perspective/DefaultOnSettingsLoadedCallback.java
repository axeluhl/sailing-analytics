package com.sap.sse.gwt.client.shared.perspective;

import com.sap.sse.common.settings.Settings;

public abstract class DefaultOnSettingsLoadedCallback<S extends Settings> implements OnSettingsLoadedCallback<S> {
    @Override
    public void onError(Throwable caught, S fallbackDefaultSettings) {
        onSuccess(fallbackDefaultSettings);
    }

}
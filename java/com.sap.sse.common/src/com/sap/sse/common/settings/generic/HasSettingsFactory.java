package com.sap.sse.common.settings.generic;

import com.sap.sse.common.settings.Settings;

public interface HasSettingsFactory<T extends Settings> {
    SettingsFactory<T> getSettingsFactory();
}

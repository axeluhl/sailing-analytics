package com.sap.sse.common.settings;

public interface HasSettingsFactory<T> {
    SettingsFactory<T> getSettingsFactory();
}

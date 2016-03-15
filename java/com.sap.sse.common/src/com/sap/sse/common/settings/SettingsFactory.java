package com.sap.sse.common.settings;

public interface SettingsFactory<T> {
    T newInstance();
}

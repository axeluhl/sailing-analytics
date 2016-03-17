package com.sap.sse.common.settings;

public interface SettingsFactory<T extends Settings> {
    T newInstance();
}

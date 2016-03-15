package com.sap.sse.common.settings;

public interface ValueSetting<T> extends HasValueSetting<T> {
    T getValue();

    void setValue(T value);
}

package com.sap.sse.common.settings.generic;

public interface ValueSetting<T> extends HasValueSetting<T> {
    T getValue();

    void setValue(T value);
    
    void setDefaultValue(T defaultValue);
    
    T getDefaultValue();
}

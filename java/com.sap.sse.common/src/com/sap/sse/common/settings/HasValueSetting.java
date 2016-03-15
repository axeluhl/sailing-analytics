package com.sap.sse.common.settings;

public interface HasValueSetting<T> extends Setting {
    ValueConverter<T> getValueConverter();
}

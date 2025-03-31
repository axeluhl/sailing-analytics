package com.sap.sse.common.settings.generic;

public interface HasValueSetting<T> extends Setting {
    ValueConverter<T> getValueConverter();
}

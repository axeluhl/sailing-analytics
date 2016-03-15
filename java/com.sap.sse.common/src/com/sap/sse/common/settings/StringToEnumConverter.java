package com.sap.sse.common.settings;

public interface StringToEnumConverter<T extends Enum<T>> {
    T fromString(String stringValue);
}

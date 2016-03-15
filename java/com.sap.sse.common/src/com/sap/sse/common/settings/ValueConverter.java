package com.sap.sse.common.settings;

public interface ValueConverter<T> {

    Object toJSONValue(T value);

    T fromJSONValue(Object jsonValue);

    String toStringValue(T value);

    T fromStringValue(String stringValue);

}

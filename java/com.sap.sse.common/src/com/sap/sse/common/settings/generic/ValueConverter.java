package com.sap.sse.common.settings.generic;

import com.sap.sse.common.settings.value.Value;

public interface ValueConverter<T> {

    Object toJSONValue(T value);

    T fromJSONValue(Object jsonValue);

    String toStringValue(T value);

    T fromStringValue(String stringValue);
    
    Value toValue(T value);
    
    T fromValue(Value value);
}

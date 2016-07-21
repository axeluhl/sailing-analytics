package com.sap.sse.common.settings.generic.converter;

import com.sap.sse.common.settings.generic.StringToEnumConverter;
import com.sap.sse.common.settings.generic.ValueConverter;
import com.sap.sse.common.settings.value.StringValue;
import com.sap.sse.common.settings.value.Value;

public class EnumConverter<T extends Enum<T>> implements ValueConverter<T> {

    private StringToEnumConverter<T> stringToEnumConverter;

    public EnumConverter(StringToEnumConverter<T> stringToEnumConverter) {
        this.stringToEnumConverter = stringToEnumConverter;
    }

    @Override
    public Object toJSONValue(T value) {
        return value == null ? null : value.name();
    }

    @Override
    public T fromJSONValue(Object jsonValue) {
        return fromStringValue((String) jsonValue);
    }

    @Override
    public String toStringValue(T value) {
        return value == null ? null : value.name();
    }

    @Override
    public T fromStringValue(String stringValue) {
        return stringValue == null ? null : stringToEnumConverter.fromString(stringValue);
    }

    @Override
    public T fromValue(Value value) {
        return fromStringValue(((StringValue) value).getValue());
    }

    @Override
    public Value toValue(T value) {
        return new StringValue(toStringValue(value));
    }
}

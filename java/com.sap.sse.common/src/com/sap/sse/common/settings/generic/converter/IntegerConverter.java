package com.sap.sse.common.settings.generic.converter;

import com.sap.sse.common.settings.generic.ValueConverter;
import com.sap.sse.common.settings.value.IntegerValue;
import com.sap.sse.common.settings.value.Value;

public class IntegerConverter implements ValueConverter<Integer> {

    public static final IntegerConverter INSTANCE = new IntegerConverter();

    private IntegerConverter() {
    }

    @Override
    public Object toJSONValue(Integer value) {
        return toStringValue(value);
    }

    @Override
    public Integer fromJSONValue(Object jsonValue) {
        return fromStringValue((String) jsonValue);
    }

    @Override
    public String toStringValue(Integer value) {
        return value == null ? null : value.toString();
    }

    @Override
    public Integer fromStringValue(String stringValue) {
        return stringValue == null ? null : Integer.parseInt(stringValue);
    }

    @Override
    public Integer fromValue(Value value) {
        return ((IntegerValue) value).getValue();
    }

    @Override
    public Value toValue(Integer value) {
        return new IntegerValue(value);
    }
}

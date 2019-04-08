package com.sap.sse.common.settings.generic.converter;

import com.sap.sse.common.settings.generic.ValueConverter;
import com.sap.sse.common.settings.value.LongValue;
import com.sap.sse.common.settings.value.Value;

public class LongConverter implements ValueConverter<Long> {

    public static final LongConverter INSTANCE = new LongConverter();

    private LongConverter() {
    }

    @Override
    public Object toJSONValue(Long value) {
        return toStringValue(value);
    }

    @Override
    public Long fromJSONValue(Object jsonValue) {
        return fromStringValue((String) jsonValue);
    }

    @Override
    public String toStringValue(Long value) {
        return value == null ? null : value.toString();
    }

    @Override
    public Long fromStringValue(String stringValue) {
        return stringValue == null ? null : Long.parseLong(stringValue);
    }

    @Override
    public Long fromValue(Value value) {
        return ((LongValue) value).getValue();
    }

    @Override
    public Value toValue(Long value) {
        return new LongValue(value);
    }
}

package com.sap.sse.common.settings.generic.converter;

import com.sap.sse.common.settings.generic.ValueConverter;
import com.sap.sse.common.settings.value.StringValue;
import com.sap.sse.common.settings.value.Value;

public class StringConverter implements ValueConverter<String> {

    public static final StringConverter INSTANCE = new StringConverter();

    private StringConverter() {
    }

    @Override
    public Object toJSONValue(String value) {
        return value;
    }

    @Override
    public String fromJSONValue(Object jsonValue) {
        return (String) jsonValue;
    }

    @Override
    public String toStringValue(String value) {
        return value;
    }

    @Override
    public String fromStringValue(String stringValue) {
        return stringValue;
    }

    @Override
    public String fromValue(Value value) {
        return ((StringValue) value).getValue();
    }

    @Override
    public Value toValue(String value) {
        return new StringValue(value);
    }
}

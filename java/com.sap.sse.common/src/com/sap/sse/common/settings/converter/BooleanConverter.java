package com.sap.sse.common.settings.converter;

import com.sap.sse.common.settings.ValueConverter;

public class BooleanConverter implements ValueConverter<Boolean> {
    
    public static final BooleanConverter INSTANCE = new BooleanConverter();
    
    private BooleanConverter() {
    }

    @Override
    public Object toJSONValue(Boolean value) {
        return value;
    }

    @Override
    public Boolean fromJSONValue(Object jsonValue) {
        return (Boolean) jsonValue;
    }

    @Override
    public String toStringValue(Boolean value) {
        return value == null ? null : value.toString();
    }

    @Override
    public Boolean fromStringValue(String stringValue) {
        return stringValue == null ? null : Boolean.valueOf(stringValue);
    }
}

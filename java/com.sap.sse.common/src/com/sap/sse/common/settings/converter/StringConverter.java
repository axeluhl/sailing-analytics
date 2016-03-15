package com.sap.sse.common.settings.converter;

import com.sap.sse.common.settings.ValueConverter;

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

}

package com.sap.sse.common.settings.generic.converter;

import com.sap.sse.common.settings.generic.ValueConverter;
import com.sap.sse.common.settings.value.BooleanValue;
import com.sap.sse.common.settings.value.Value;

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
        // Boolean.valueOf(stringValue) is explicitly not used here
        // to ensure consistency to JSON value handling
        if (Boolean.TRUE.toString().equalsIgnoreCase(stringValue)) {
            return true;
        } else if (Boolean.FALSE.toString().equalsIgnoreCase(stringValue)) {
            return false;
        }
        throw new IllegalArgumentException("\"" + stringValue + "\" can not be parsed as boolean value");
    }

    @Override
    public Boolean fromValue(Value value) {
        return ((BooleanValue) value).getValue();
    }

    @Override
    public Value toValue(Boolean value) {
        return new BooleanValue(value);
    }
}

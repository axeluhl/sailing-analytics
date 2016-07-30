package com.sap.sse.common.settings.generic.converter;

import com.sap.sse.common.settings.generic.ValueConverter;
import com.sap.sse.common.settings.value.DoubleValue;
import com.sap.sse.common.settings.value.Value;

public class DoubleConverter implements ValueConverter<Double> {

    public static final DoubleConverter INSTANCE = new DoubleConverter();

    private DoubleConverter() {
    }

    @Override
    public Object toJSONValue(Double value) {
        return value;
    }

    @Override
    public Double fromJSONValue(Object jsonValue) {
        return (Double) jsonValue;
    }

    @Override
    public String toStringValue(Double value) {
        return value == null ? null : value.toString();
    }

    @Override
    public Double fromStringValue(String stringValue) {
        return stringValue == null ? null : Double.valueOf(stringValue);
    }

    @Override
    public Double fromValue(Value value) {
        return ((DoubleValue) value).getValue();
    }

    @Override
    public Value toValue(Double value) {
        return new DoubleValue(value);
    }
}

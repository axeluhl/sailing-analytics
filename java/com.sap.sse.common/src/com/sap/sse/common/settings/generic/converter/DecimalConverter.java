package com.sap.sse.common.settings.generic.converter;

import java.math.BigDecimal;

import com.sap.sse.common.settings.generic.ValueConverter;
import com.sap.sse.common.settings.value.DecimalValue;
import com.sap.sse.common.settings.value.Value;

public class DecimalConverter implements ValueConverter<BigDecimal> {

    public static final DecimalConverter INSTANCE = new DecimalConverter();

    private DecimalConverter() {
    }

    @Override
    public Object toJSONValue(BigDecimal value) {
        return toStringValue(value);
    }

    @Override
    public BigDecimal fromJSONValue(Object jsonValue) {
        return fromStringValue((String) jsonValue);
    }

    @Override
    public String toStringValue(BigDecimal value) {
        return value == null ? null : value.toString();
    }

    @Override
    public BigDecimal fromStringValue(String stringValue) {
        return stringValue == null ? null : new BigDecimal(stringValue);
    }

    @Override
    public BigDecimal fromValue(Value value) {
        return ((DecimalValue) value).getValue();
    }

    @Override
    public Value toValue(BigDecimal value) {
        return new DecimalValue(value);
    }
}

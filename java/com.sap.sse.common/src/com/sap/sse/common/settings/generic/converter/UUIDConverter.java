package com.sap.sse.common.settings.generic.converter;

import java.util.UUID;

import com.sap.sse.common.settings.generic.ValueConverter;
import com.sap.sse.common.settings.value.UUIDValue;
import com.sap.sse.common.settings.value.Value;

public class UUIDConverter implements ValueConverter<UUID> {

    public static final UUIDConverter INSTANCE = new UUIDConverter();

    private UUIDConverter() {
    }

    @Override
    public Object toJSONValue(UUID value) {
        return toStringValue(value);
    }

    @Override
    public UUID fromJSONValue(Object jsonValue) {
        return fromStringValue((String) jsonValue);
    }

    @Override
    public String toStringValue(UUID value) {
        return value == null ? null : value.toString();
    }

    @Override
    public UUID fromStringValue(String stringValue) {
        return stringValue == null ? null : UUID.fromString(stringValue);
    }

    @Override
    public UUID fromValue(Value value) {
        return ((UUIDValue) value).getValue();
    }

    @Override
    public Value toValue(UUID value) {
        return new UUIDValue(value);
    }
}

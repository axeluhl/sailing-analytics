package com.sap.sailing.gwt.common.settings.converter;

import com.sap.sse.common.Duration;
import com.sap.sse.common.impl.MillisecondsDurationImpl;
import com.sap.sse.common.settings.generic.ValueConverter;
import com.sap.sse.common.settings.value.LongValue;
import com.sap.sse.common.settings.value.Value;

public class DurationConverter implements ValueConverter<Duration> {

    public static final DurationConverter INSTANCE = new DurationConverter();

    private DurationConverter() {
    }

    @Override
    public Object toJSONValue(Duration value) {
        return toStringValue(value);
    }

    @Override
    public Duration fromJSONValue(Object jsonValue) {
        return fromStringValue((String) jsonValue);
    }

    @Override
    public String toStringValue(Duration value) {
        return value == null ? null : Long.toString(value.asMillis());
    }

    @Override
    public Duration fromStringValue(String stringValue) {
        return stringValue == null ? null : new MillisecondsDurationImpl(Long.parseLong(stringValue));
    }

    @Override
    public Duration fromValue(Value value) {
        return new MillisecondsDurationImpl(((LongValue) value).getValue());
    }

    @Override
    public Value toValue(Duration value) {
        return new LongValue(value.asMillis());
    }
}

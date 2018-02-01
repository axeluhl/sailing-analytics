package com.sap.sailing.gwt.settings.client.settingtypes.converter;

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
        // String value is used in URL where we accept the formats matching [[hh:]mm:]ss
        // and the number of digits per element is not restricted to 2.
        // This makes an integer number to simply be parsed as seconds value.
        return value == null ? null : Long.toString((long)value.asSeconds());
    }

    @Override
    public Duration fromStringValue(String stringValue) {
        return stringValue == null ? null : parseDuration(stringValue);
    }

    @Override
    public Duration fromValue(Value value) {
        LongValue longValue = (LongValue) value;
        return longValue.getValue() == null ? null : new MillisecondsDurationImpl(longValue.getValue());
    }

    @Override
    public Value toValue(Duration value) {
        return value == null ? null : new LongValue(value.asMillis());
    }
    
    /**
     * Understands [[hh:]mm:]ss and parses into a {@link Duration}. If {@code durationAsString} is {@code null} then
     * so is the result.
     */
    private static Duration parseDuration(String durationAsString) {
        final Duration result;
        if (durationAsString == null) {
            result = null;
        } else {
            long seconds = 0;
            for (final String hhmmss : durationAsString.split(":")) {
                seconds = 60*seconds + Long.valueOf(hhmmss);
            }
            result = new MillisecondsDurationImpl(1000l * seconds);
        }
        return result;
    }
}

package com.sap.sse.common.settings.value;

import com.sap.sse.common.settings.generic.ValueConverter;

public interface ValueCollection extends Value {
    <T> Iterable<T> getValues(ValueConverter<T> converter);

    <T> void setValues(Iterable<T> values, ValueConverter<T> converter);

    void clear();
}

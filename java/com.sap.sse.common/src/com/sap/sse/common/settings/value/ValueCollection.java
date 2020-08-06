package com.sap.sse.common.settings.value;

import com.sap.sse.common.settings.generic.ValueConverter;

public interface ValueCollection extends Value {
    /**
     * Obtains a non-live copy of the values in this collection. Modifications to this value collection
     * will not reflect in the result of this method, and vice versa.
     */
    <T> Iterable<T> getValues(ValueConverter<T> converter);

    <T> void setValues(Iterable<T> values, ValueConverter<T> converter);

    void clear();
}

package com.sap.sse.common.settings.generic;

public interface ValueSetSetting<T> extends ValueCollectionSetting<T> {
    void setDefaultValues(Iterable<T> defaultValues);
}

package com.sap.sse.common.settings;

public interface ValueSetSetting<T> extends ValueCollectionSetting<T> {
    void setDefaultValues(Iterable<T> defaultValues);
}

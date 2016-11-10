package com.sap.sse.common.settings.generic;

public interface ValueListSetting<T> extends ValueCollectionSetting<T> {
    void setDefaultValues(Iterable<T> defaultValues);
}

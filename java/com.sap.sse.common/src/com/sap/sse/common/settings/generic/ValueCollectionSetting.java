package com.sap.sse.common.settings.generic;


public interface ValueCollectionSetting<T> extends HasValueSetting<T>, CollectionSetting<T> {
    void setDefaultValues(Iterable<T> defaultValues);
    Iterable<T> getDefaultValues();
    boolean isValuesEmpty();
    void setDiff(Iterable<T> removedValues, Iterable<T> addedValues);
    Iterable<T> getRemovedValues();
    Iterable<T> getAddedValues();
}

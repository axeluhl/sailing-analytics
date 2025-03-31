package com.sap.sse.common.settings.generic;


public interface ValueCollectionSetting<T> extends HasValueSetting<T>, CollectionSetting<T> {
    void setDefaultValues(Iterable<T> defaultValues);
    
    /**
     * Obtains a non-live copy of the default values. Modification to this object's default values hence will
     * not reflect into the iterable returned.
     */
    Iterable<T> getDefaultValues();
    boolean isValuesEmpty();
    void setDiff(Iterable<T> removedValues, Iterable<T> addedValues);
    Iterable<T> getRemovedValues();
    Iterable<T> getAddedValues();
}

package com.sap.sse.common.settings.generic;


public interface CollectionSetting<T> extends Setting {
    
    void setValues(Iterable<T> values);
    
    /**
     * Obtains a non-live copy of the values in this collection setting. Changes to this setting
     * will not reflect in the iterable returned.
     */
    Iterable<T> getValues();

    void addValue(T value);

}

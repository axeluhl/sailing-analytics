package com.sap.sse.common.settings.generic;


public interface CollectionSetting<T> extends Setting {
    
    void setValues(Iterable<T> values);
    
    Iterable<T> getValues();

    void addValue(T value);

}

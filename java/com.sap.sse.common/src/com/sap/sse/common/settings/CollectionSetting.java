package com.sap.sse.common.settings;


public interface CollectionSetting<T> extends Setting {
    
    void setValues(Iterable<T> values);
    
    Iterable<T> getValues();

}

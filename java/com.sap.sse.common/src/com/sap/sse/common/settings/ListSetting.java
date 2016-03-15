package com.sap.sse.common.settings;


public interface ListSetting<T> extends Setting {
    
    void setValues(Iterable<T> values);
    
    Iterable<T> getValues();

}

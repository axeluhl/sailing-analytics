package com.sap.sse.datamining.components;

public interface FilterCriteria<T> {
    
    public boolean matches(T element);

}

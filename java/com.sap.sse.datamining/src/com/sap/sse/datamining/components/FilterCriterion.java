package com.sap.sse.datamining.components;

public interface FilterCriterion<T> {
    
    public boolean matches(T element);

}

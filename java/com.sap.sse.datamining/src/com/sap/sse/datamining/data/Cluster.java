package com.sap.sse.datamining.data;

import java.util.Comparator;

public interface Cluster<ValueType> {
    
    public String getName();
    public ValueType getUpperRange();
    public ValueType getLowerRange();
    
    public boolean isInRange(ValueType value, Comparator<ValueType> comparator);

}

package com.sap.sailing.datamining;

import java.util.Comparator;

public interface Cluster<ValueType> {
    
    public boolean isInRange(ValueType value, Comparator<ValueType> comparator);

}

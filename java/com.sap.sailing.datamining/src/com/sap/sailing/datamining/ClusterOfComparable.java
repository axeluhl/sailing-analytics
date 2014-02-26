package com.sap.sailing.datamining;

public interface ClusterOfComparable<ValueType extends Comparable<ValueType>> extends Cluster<ValueType> {
    
    public boolean isInRange(ValueType value);

}

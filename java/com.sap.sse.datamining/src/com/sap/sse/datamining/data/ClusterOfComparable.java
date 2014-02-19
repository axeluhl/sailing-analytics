package com.sap.sse.datamining.data;

public interface ClusterOfComparable<ValueType extends Comparable<ValueType>> extends Cluster<ValueType> {
    
    public boolean isInRange(ValueType value);

}

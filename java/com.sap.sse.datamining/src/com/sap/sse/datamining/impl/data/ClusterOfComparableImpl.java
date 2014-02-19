package com.sap.sse.datamining.impl.data;

import com.sap.sse.datamining.data.ClusterOfComparable;

public class ClusterOfComparableImpl<ValueType extends Comparable<ValueType>> extends ClusterImpl<ValueType> implements
        ClusterOfComparable<ValueType> {

    public ClusterOfComparableImpl(String name, ValueType upperRange, ValueType lowerRange) {
        super(name, upperRange, lowerRange);
    }

    @Override
    public boolean isInRange(ValueType value) {
        return value != null && value.compareTo(getLowerRange()) >= 0 && value.compareTo(getUpperRange()) <= 0;
    }

}

package com.sap.sailing.datamining.impl;

import com.sap.sailing.datamining.ClusterOfComparable;

public class ClusterOfComparableImpl<ValueType extends Comparable<ValueType>> extends ClusterImpl<ValueType> implements
        ClusterOfComparable<ValueType> {

    public ClusterOfComparableImpl(ValueType upperRange, ValueType lowerRange) {
        super(upperRange, lowerRange);
    }

    @Override
    public boolean isInRange(ValueType value) {
        return value.compareTo(getLowerRange()) >= 0 && value.compareTo(getUpperRange()) <= 0;
    }

}

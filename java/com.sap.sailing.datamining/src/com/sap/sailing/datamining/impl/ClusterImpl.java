package com.sap.sailing.datamining.impl;

import java.util.Comparator;

import com.sap.sailing.datamining.Cluster;

public class ClusterImpl<ValueType> implements Cluster<ValueType> {

    private ValueType upperRange;
    private ValueType lowerRange;

    public ClusterImpl(ValueType upperRange, ValueType lowerRange) {
        this.upperRange = upperRange;
        this.lowerRange = lowerRange;
    }

    @Override
    public boolean isInRange(ValueType value, Comparator<ValueType> comparator) {
        return comparator.compare(value, lowerRange) >= 0 && comparator.compare(value, upperRange) <= 0;
    }

    protected ValueType getUpperRange() {
        return upperRange;
    }

    protected ValueType getLowerRange() {
        return lowerRange;
    }

}

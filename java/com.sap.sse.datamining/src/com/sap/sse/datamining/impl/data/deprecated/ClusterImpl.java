package com.sap.sse.datamining.impl.data.deprecated;

import java.util.Comparator;

import com.sap.sse.datamining.data.deprecated.Cluster;

public class ClusterImpl<ValueType> implements Cluster<ValueType> {

    private String name;
    private ValueType upperRange;
    private ValueType lowerRange;

    public ClusterImpl(String name, ValueType upperRange, ValueType lowerRange) {
        this.name = name;
        this.upperRange = upperRange;
        this.lowerRange = lowerRange;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ValueType getUpperRange() {
        return upperRange;
    }

    @Override
    public ValueType getLowerRange() {
        return lowerRange;
    }

    @Override
    public boolean isInRange(ValueType value, Comparator<ValueType> comparator) {
        return comparator.compare(value, getLowerRange()) >= 0 && comparator.compare(value, getUpperRange()) <= 0;
    }

}

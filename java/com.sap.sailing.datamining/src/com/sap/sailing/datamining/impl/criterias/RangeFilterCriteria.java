package com.sap.sailing.datamining.impl.criterias;

import com.sap.sailing.datamining.ClusterOfComparable;
import com.sap.sailing.datamining.ConcurrentFilterCriteria;

public abstract class RangeFilterCriteria<DataType, ValueType extends Comparable<ValueType>> implements ConcurrentFilterCriteria<DataType> {
    
    private ClusterOfComparable<ValueType> cluster;

    public RangeFilterCriteria(ClusterOfComparable<ValueType> cluster) {
        this.cluster = cluster;
    }

    @Override
    public boolean matches(DataType data) {
        return isInRange(data);
    }

    protected boolean isInRange(DataType data) {
        return cluster.isInRange(getValue(data));
    }

    public abstract ValueType getValue(DataType data);

}

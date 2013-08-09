package com.sap.sailing.datamining.impl;

import com.sap.sailing.datamining.ClusterOfComparable;
import com.sap.sailing.datamining.impl.criterias.RangeFilterCriteria;

public class SimpleRangeFilterCriteria<DataType extends Comparable<DataType>> extends RangeFilterCriteria<DataType, DataType> {

    public SimpleRangeFilterCriteria(ClusterOfComparable<DataType> cluster) {
        super(cluster);
    }

    @Override
    public DataType getValue(DataType data) {
        return data;
    }

}

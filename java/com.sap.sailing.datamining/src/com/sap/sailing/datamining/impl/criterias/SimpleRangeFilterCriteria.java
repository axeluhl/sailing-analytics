package com.sap.sailing.datamining.impl.criterias;

import com.sap.sse.datamining.data.ClusterOfComparable;

public class SimpleRangeFilterCriteria<DataType extends Comparable<DataType>> extends RangeFilterCriteria<DataType, DataType> {

    public SimpleRangeFilterCriteria(ClusterOfComparable<DataType> cluster) {
        super(cluster);
    }

    @Override
    public DataType getValue(DataType data) {
        return data;
    }

}

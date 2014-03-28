package com.sap.sse.datamining.impl.criterias.deprecated;

import com.sap.sse.datamining.data.deprecated.ClusterOfComparable;

public class SimpleRangeFilterCriteria<DataType extends Comparable<DataType>> extends RangeFilterCriteria<DataType, DataType> {

    public SimpleRangeFilterCriteria(ClusterOfComparable<DataType> cluster) {
        super(cluster);
    }

    @Override
    public DataType getValue(DataType data) {
        return data;
    }

}

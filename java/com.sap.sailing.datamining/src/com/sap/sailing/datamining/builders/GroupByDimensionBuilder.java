package com.sap.sailing.datamining.builders;

import java.util.Collection;

import com.sap.sailing.datamining.Dimension;
import com.sap.sailing.datamining.GroupingWorker;
import com.sap.sailing.datamining.WorkerBuilder;
import com.sap.sailing.datamining.impl.GroupByDimension;

public class GroupByDimensionBuilder<DataType, ValueType> implements WorkerBuilder<GroupingWorker<DataType>> {

    private Collection<Dimension<DataType, ValueType>> dimensions;

    public GroupByDimensionBuilder(Collection<Dimension<DataType, ValueType>> dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    public GroupingWorker<DataType> build() {
        return new GroupByDimension<DataType, ValueType>(dimensions);
    }

}

package com.sap.sailing.datamining.builders;

import java.util.Collection;

import com.sap.sailing.datamining.Dimension;
import com.sap.sailing.datamining.GroupingWorker;
import com.sap.sailing.datamining.WorkerBuilder;
import com.sap.sailing.datamining.impl.MultiDimensionalGroupingWorker;

public class MultiDimensionalGroupingWorkerBuilder<DataType, ValueType> implements WorkerBuilder<GroupingWorker<DataType>> {

    private Collection<Dimension<DataType, ValueType>> dimensions;

    public MultiDimensionalGroupingWorkerBuilder(Collection<Dimension<DataType, ValueType>> dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    public GroupingWorker<DataType> build() {
        return new MultiDimensionalGroupingWorker<DataType, ValueType>(dimensions);
    }

}

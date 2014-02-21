package com.sap.sse.datamining.impl.workers.builders;

import java.util.Collection;

import com.sap.sse.datamining.data.Dimension;
import com.sap.sse.datamining.impl.workers.MultiDimensionalGroupingWorker;
import com.sap.sse.datamining.workers.GroupingWorker;
import com.sap.sse.datamining.workers.WorkerBuilder;

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

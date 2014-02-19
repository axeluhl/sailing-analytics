package com.sap.sailing.datamining.factories;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import com.sap.sailing.datamining.builders.MultiDimensionalGroupingWorkerBuilder;
import com.sap.sailing.datamining.dimensions.DimensionManager;
import com.sap.sailing.datamining.dimensions.DimensionManagerProvider;
import com.sap.sailing.datamining.impl.PartitioningParallelGrouper;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.datamining.shared.DimensionIdentifier;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sse.datamining.components.ParallelGrouper;
import com.sap.sse.datamining.data.Dimension;
import com.sap.sse.datamining.workers.GroupingWorker;
import com.sap.sse.datamining.workers.WorkerBuilder;

public final class GrouperFactory {
    
    private GrouperFactory() { }

    public static <DataType> ParallelGrouper<DataType> createGrouper(QueryDefinition queryDefinition, ThreadPoolExecutor executor) {
        WorkerBuilder<GroupingWorker<DataType>> workerBuilder = createGroupingWorkerBuilder(queryDefinition);
        return new PartitioningParallelGrouper<DataType>(workerBuilder, executor);
    }

    private static <DataType> WorkerBuilder<GroupingWorker<DataType>> createGroupingWorkerBuilder(QueryDefinition queryDefinition) {
        switch (queryDefinition.getGrouperType()) {
        case Dimensions:
            return createMultiDimensionalGroupingWorkerBuilder(queryDefinition.getDataType(), queryDefinition.getDimensionsToGroupBy());
        }
        throw new IllegalArgumentException("Not yet implemented for the given grouper type: "
                + queryDefinition.getGrouperType().toString());
    }

    @SuppressWarnings("unchecked")
    private static <DataType, ValueType> WorkerBuilder<GroupingWorker<DataType>> createMultiDimensionalGroupingWorkerBuilder(DataTypes dataType, List<DimensionIdentifier> dimensionsToGroupBy) {
        DimensionManager<DataType> dimensionManager = DimensionManagerProvider.getDimensionManagerFor(dataType);
        Collection<Dimension<DataType, ValueType>> dimensions = new LinkedHashSet<Dimension<DataType, ValueType>>();
        for (DimensionIdentifier sharedDimension : dimensionsToGroupBy) {
            Dimension<DataType, ValueType> dimension = (Dimension<DataType, ValueType>) dimensionManager.getDimensionFor(sharedDimension);
            dimensions.add(dimension);
        }
        return new MultiDimensionalGroupingWorkerBuilder<DataType, ValueType>(dimensions);
    }

}

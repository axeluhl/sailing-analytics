package com.sap.sailing.datamining.factories;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import com.sap.sailing.datamining.BaseBindingProvider;
import com.sap.sailing.datamining.Dimension;
import com.sap.sailing.datamining.GroupingWorker;
import com.sap.sailing.datamining.ParallelGrouper;
import com.sap.sailing.datamining.SimpleParallelGrouper;
import com.sap.sailing.datamining.WorkerBuilder;
import com.sap.sailing.datamining.builders.DynamicGrouperBuilder;
import com.sap.sailing.datamining.builders.GroupByDimensionBuilder;
import com.sap.sailing.datamining.dimensions.DimensionManager;
import com.sap.sailing.datamining.dimensions.DimensionManagerProvider;
import com.sap.sailing.datamining.impl.gpsfix.GPSFixBaseBindingProvider;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.SharedDimension;

public final class GrouperFactory {
    
    private GrouperFactory() { }

    public static <DataType> ParallelGrouper<DataType> createGrouper(QueryDefinition queryDefinition, ThreadPoolExecutor executor) {
        WorkerBuilder<GroupingWorker<DataType>> workerBuilder = createGroupingWorkerBuilder(queryDefinition);
        return new SimpleParallelGrouper<DataType>(workerBuilder, executor);
    }

    private static <DataType> WorkerBuilder<GroupingWorker<DataType>> createGroupingWorkerBuilder(QueryDefinition queryDefinition) {
        switch (queryDefinition.getGrouperType()) {
        case Custom:
            return createDynamicGrouperBuilder(queryDefinition.getCustomGrouperScriptText(), queryDefinition.getDataType());
        case Dimensions:
            return createByDimensionGrouperBuilder(queryDefinition.getDataType(), queryDefinition.getDimensionsToGroupBy());
        }
        throw new IllegalArgumentException("Not yet implemented for the given grouper type: "
                + queryDefinition.getGrouperType().toString());
    }

    public static <DataType> WorkerBuilder<GroupingWorker<DataType>> createDynamicGrouperBuilder(String grouperScriptText, DataTypes dataType) {
        BaseBindingProvider<DataType> baseBindingProvider = createBaseBindingProvider(dataType);
        return new DynamicGrouperBuilder<DataType>(grouperScriptText, baseBindingProvider);
    }

    @SuppressWarnings("unchecked")
    private static <DataType> BaseBindingProvider<DataType> createBaseBindingProvider(DataTypes dataType) {
        switch (dataType) {
        case GPSFix:
            return (BaseBindingProvider<DataType>) new GPSFixBaseBindingProvider();
        }
        throw new IllegalArgumentException("Not yet implemented for the given data type: "
                + dataType.toString());
    }

    @SuppressWarnings("unchecked")
    private static <DataType, ValueType> WorkerBuilder<GroupingWorker<DataType>> createByDimensionGrouperBuilder(DataTypes dataType, List<SharedDimension> dimensionsToGroupBy) {
        DimensionManager<DataType> dimensionManager = DimensionManagerProvider.getDimensionManagerFor(dataType);
        Collection<Dimension<DataType, ValueType>> dimensions = new LinkedHashSet<Dimension<DataType, ValueType>>();
        for (SharedDimension sharedDimension : dimensionsToGroupBy) {
            Dimension<DataType, ValueType> dimension = (Dimension<DataType, ValueType>) dimensionManager.getDimensionFor(sharedDimension);
            dimensions.add(dimension);
        }
        return new GroupByDimensionBuilder<DataType, ValueType>(dimensions);
    }

}

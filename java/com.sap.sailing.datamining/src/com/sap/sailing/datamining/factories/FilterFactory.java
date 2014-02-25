package com.sap.sailing.datamining.factories;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;

import com.sap.sailing.datamining.dimensions.DimensionManager;
import com.sap.sailing.datamining.dimensions.DimensionManagerProvider;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.datamining.shared.DimensionIdentifier;
import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.components.ParallelFilter;
import com.sap.sse.datamining.data.Dimension;
import com.sap.sse.datamining.impl.components.deprecated.NonFilteringFilter;
import com.sap.sse.datamining.impl.components.deprecated.PartitioningParallelFilter;
import com.sap.sse.datamining.impl.criterias.AndCompoundFilterCriteria;
import com.sap.sse.datamining.impl.criterias.CompoundFilterCriteria;
import com.sap.sse.datamining.impl.criterias.deprecated.DimensionValuesFilterCriteria;
import com.sap.sse.datamining.impl.workers.builders.FilterByCriteriaBuilder;
import com.sap.sse.datamining.workers.FiltrationWorker;
import com.sap.sse.datamining.workers.WorkerBuilder;

public final class FilterFactory {

    private FilterFactory() { }
    
    public static <DataType> ParallelFilter<DataType> createParallelFilter(WorkerBuilder<FiltrationWorker<DataType>> workerBuilder, ThreadPoolExecutor executor) {
        return new PartitioningParallelFilter<DataType>(workerBuilder, executor);
    }

    public static <DataType> WorkerBuilder<FiltrationWorker<DataType>> createDimensionFilterBuilder(DataTypes dataType, Map<DimensionIdentifier, Iterable<?>> selection) {
        FilterCriteria<DataType> criteria = createAndCompoundDimensionFilterCritera(dataType, selection);
        WorkerBuilder<FiltrationWorker<DataType>> builder = new FilterByCriteriaBuilder<DataType>(criteria); 
        return builder;
    }

    /**
     * @return A filter that filters nothing. So the returning collection is the same as the given one.
     */
    public static <DataType> ParallelFilter<DataType> createNonFilteringFilter() {
        return new NonFilteringFilter<DataType>();
    }

    private static <DataType> FilterCriteria<DataType> createAndCompoundDimensionFilterCritera(DataTypes dataType, Map<DimensionIdentifier, Iterable<?>> selection) {
        DimensionManager<DataType> dimensionManager = DimensionManagerProvider.getDimensionManagerFor(dataType);
        CompoundFilterCriteria<DataType> compoundCriteria = new AndCompoundFilterCriteria<DataType>();

        for (Entry<DimensionIdentifier, Iterable<?>> entry : selection.entrySet()) {
            Dimension<DataType, ?> dimension = dimensionManager.getDimensionFor(entry.getKey());
            if (dimension != null) {
                FilterCriteria<DataType> criteria = createDimensionFilterCriteria(dimension, entry.getValue());
                compoundCriteria.addCriteria(criteria);
            }
        }
        return compoundCriteria;
    }
    
    @SuppressWarnings("unchecked")
    private static <DataType, ValueType> FilterCriteria<DataType> createDimensionFilterCriteria(Dimension<DataType, ValueType> dimension, Iterable<?> values) {
        return new DimensionValuesFilterCriteria<DataType, ValueType>(dimension, (Collection<ValueType>) values);
    }

}

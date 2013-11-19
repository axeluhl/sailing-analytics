package com.sap.sailing.datamining.factories;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;

import com.sap.sailing.datamining.Dimension;
import com.sap.sailing.datamining.ParallelFilter;
import com.sap.sailing.datamining.ConcurrentFilterCriteria;
import com.sap.sailing.datamining.FiltrationWorker;
import com.sap.sailing.datamining.WorkerBuilder;
import com.sap.sailing.datamining.builders.FilterByCriteriaBuilder;
import com.sap.sailing.datamining.data.impl.NoFilter;
import com.sap.sailing.datamining.dimensions.DimensionManager;
import com.sap.sailing.datamining.dimensions.DimensionManagerProvider;
import com.sap.sailing.datamining.impl.SimpleParallelFilter;
import com.sap.sailing.datamining.impl.criterias.AndCompoundFilterCriteria;
import com.sap.sailing.datamining.impl.criterias.CompoundFilterCriteria;
import com.sap.sailing.datamining.impl.criterias.DimensionValuesFilterCriteria;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.datamining.shared.SharedDimension;

public final class FilterFactory {

    private FilterFactory() { }
    
    public static <DataType> ParallelFilter<DataType> createParallelFilter(WorkerBuilder<FiltrationWorker<DataType>> workerBuilder, ThreadPoolExecutor executor) {
        return new SimpleParallelFilter<DataType>(workerBuilder, executor);
    }

    public static <DataType> WorkerBuilder<FiltrationWorker<DataType>> createDimensionFilterBuilder(DataTypes dataType, Map<SharedDimension, Iterable<?>> selection) {
        ConcurrentFilterCriteria<DataType> criteria = createAndCompoundDimensionFilterCritera(dataType, selection);
        WorkerBuilder<FiltrationWorker<DataType>> builder = createCriteriaFilterBuilder(criteria); 
        return builder;
    }

    /**
     * @return A filter that filters nothing. So the returning collection is the same as the given one.
     */
    public static <DataType> ParallelFilter<DataType> createNoFilter() {
        return new NoFilter<DataType>();
    }

    public static <DataType> WorkerBuilder<FiltrationWorker<DataType>> createCriteriaFilterBuilder(ConcurrentFilterCriteria<DataType> criteria) {
        return new FilterByCriteriaBuilder<DataType>(criteria);
    }

    private static <DataType> ConcurrentFilterCriteria<DataType> createAndCompoundDimensionFilterCritera(DataTypes dataType, Map<SharedDimension, Iterable<?>> selection) {
        DimensionManager<DataType> dimensionManager = DimensionManagerProvider.getDimensionManagerFor(dataType);
        CompoundFilterCriteria<DataType> compoundCriteria = new AndCompoundFilterCriteria<DataType>();

        for (Entry<SharedDimension, Iterable<?>> entry : selection.entrySet()) {
            Dimension<DataType, ?> dimension = dimensionManager.getDimensionFor(entry.getKey());
            if (dimension != null) {
                ConcurrentFilterCriteria<DataType> criteria = createDimensionFilterCriteria(dimension, entry.getValue());
                compoundCriteria.addCriteria(criteria);
            }
        }
        return compoundCriteria;
    }
    
    @SuppressWarnings("unchecked")
    private static <DataType, ValueType> ConcurrentFilterCriteria<DataType> createDimensionFilterCriteria(Dimension<DataType, ValueType> dimension, Iterable<?> values) {
        return new DimensionValuesFilterCriteria<DataType, ValueType>(dimension, (Collection<ValueType>) values);
    }

}

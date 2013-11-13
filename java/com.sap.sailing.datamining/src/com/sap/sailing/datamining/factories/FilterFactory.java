package com.sap.sailing.datamining.factories;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;

import com.sap.sailing.datamining.Dimension;
import com.sap.sailing.datamining.Filter;
import com.sap.sailing.datamining.FilterCriteria;
import com.sap.sailing.datamining.data.impl.NoFilter;
import com.sap.sailing.datamining.dimensions.DimensionManager;
import com.sap.sailing.datamining.dimensions.DimensionManagerProvider;
import com.sap.sailing.datamining.impl.FilterByCriteria;
import com.sap.sailing.datamining.impl.ParallelFilter;
import com.sap.sailing.datamining.impl.SingleThreadedFilter;
import com.sap.sailing.datamining.impl.criterias.AndCompoundFilterCriteria;
import com.sap.sailing.datamining.impl.criterias.CompoundFilterCriteria;
import com.sap.sailing.datamining.impl.criterias.DimensionValuesFilterCriteria;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.datamining.shared.SharedDimension;

public final class FilterFactory {

    private FilterFactory() { }
    
    public static <DataType> Filter<DataType> createParallelFilter(SingleThreadedFilter<DataType> workerBase, ThreadPoolExecutor executor) {
        return new ParallelFilter<DataType>(workerBase, executor);
    }

    public static <DataType> SingleThreadedFilter<DataType> createDimensionFilter(DataTypes dataType, Map<SharedDimension, Iterable<?>> selection) {
        FilterCriteria<DataType> criteria = createAndCompoundDimensionFilterCritera(dataType, selection);
        SingleThreadedFilter<DataType> filter = createCriteriaFilter(criteria); 
        return filter;
    }

    /**
     * @return A filter that filters nothing. So the returning collection is the same as the given one.
     */
    public static <DataType> Filter<DataType> createNoFilter() {
        return new NoFilter<DataType>();
    }

    private static <DataType> FilterCriteria<DataType> createAndCompoundDimensionFilterCritera(DataTypes dataType, Map<SharedDimension, Iterable<?>> selection) {
        DimensionManager<DataType> dimensionManager = DimensionManagerProvider.getDimensionManagerFor(dataType);
        CompoundFilterCriteria<DataType> compoundCriteria = new AndCompoundFilterCriteria<DataType>();

        for (Entry<SharedDimension, Iterable<?>> entry : selection.entrySet()) {
            Dimension<DataType, ?> dimension = dimensionManager.getDimensionFor(entry.getKey());
            if (dimension != null) {
                FilterCriteria<DataType> criteria = createDimensionFilterCriteria(dimension, entry.getValue());
                compoundCriteria.addCriteria(criteria);
            }
        }
        return compoundCriteria;
    }

    public static <DataType> SingleThreadedFilter<DataType> createCriteriaFilter(FilterCriteria<DataType> criteria) {
        return new FilterByCriteria<DataType>(criteria);
    }
    
    @SuppressWarnings("unchecked")
    public static <DataType, ValueType> FilterCriteria<DataType> createDimensionFilterCriteria(Dimension<DataType, ValueType> dimension, Iterable<?> values) {
        return new DimensionValuesFilterCriteria<DataType, ValueType>(dimension, (Collection<ValueType>) values);
    }

}

package com.sap.sailing.datamining;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import com.sap.sailing.datamining.impl.DataAmountExtractor;
import com.sap.sailing.datamining.impl.FilterByCriteriaImpl;
import com.sap.sailing.datamining.impl.QueryImpl;
import com.sap.sailing.datamining.impl.aggregators.DoubleAverageAggregator;
import com.sap.sailing.datamining.impl.aggregators.DoubleSumAggregator;
import com.sap.sailing.datamining.impl.aggregators.IntegerAverageAggregator;
import com.sap.sailing.datamining.impl.aggregators.IntegerSumAggregator;
import com.sap.sailing.datamining.impl.criterias.AndCompoundFilterCriteria;
import com.sap.sailing.datamining.impl.criterias.CompoundFilterCriteria;
import com.sap.sailing.datamining.impl.criterias.DimensionValuesFilterCriteria;
import com.sap.sailing.datamining.impl.gpsfix.GPSFixRetrieverImpl;
import com.sap.sailing.datamining.impl.gpsfix.GroupGPSFixesByDimension;
import com.sap.sailing.datamining.shared.AggregatorType;
import com.sap.sailing.datamining.shared.SharedDimensions;

public class DataMiningFactory {
    
    private DataMiningFactory() { }

    public static <DataType, ExtractedType, AggregatedType> Query<DataType, AggregatedType> createQuery(
            DataRetriever<DataType> retriever, Filter<DataType> filter, Grouper<DataType> grouper,
            Extractor<DataType, ExtractedType> extractor, Aggregator<ExtractedType, AggregatedType> aggregator) {
        return new QueryImpl<DataType, ExtractedType, AggregatedType>(retriever, filter, grouper, extractor, aggregator);
    }
    
    /**
     * Creates a retriever for the given data type. Throws an exception, if the used <code>DataType</code> doesn't match the <code>DataType</code> of the returning retriever.
     */
    @SuppressWarnings("unchecked")
    public static <DataType> DataRetriever<DataType> createDataRetriever(com.sap.sailing.datamining.shared.DataType dataType) {
        switch (dataType) {
        case GPSFix:
            return (DataRetriever<DataType>) new GPSFixRetrieverImpl();
        }
        throw new IllegalArgumentException("Not yet implemented for the given data type: "
                + dataType.toString());
    }

    public static DataRetriever<GPSFixWithContext> createGPSFixRetriever() {
        return new GPSFixRetrieverImpl();
    }
    
    /**
     * @return A filter that filters nothing. So the returning collection is the same as the given one.
     */
    public static <DataType> Filter<DataType> createNoFilter() {
        return new Filter<DataType>() {
            @Override
            public Collection<DataType> filter(Collection<DataType> data) {
                return data;
            }
        };
    }

    public static <DataType> Filter<DataType> createCriteriaFilter(FilterCriteria<DataType> criteria) {
        return new FilterByCriteriaImpl<DataType>(criteria);
    }

    public static FilterCriteria<GPSFixWithContext> createGPSFixDimensionFilterCriteria(Map<SharedDimensions.GPSFix, Collection<?>> selection) {
        CompoundFilterCriteria<GPSFixWithContext> compoundFilterCriteria = new AndCompoundFilterCriteria<GPSFixWithContext>();
        for (Entry<SharedDimensions.GPSFix, Collection<?>> selectionEntry : selection.entrySet()) {
            FilterCriteria<GPSFixWithContext> criteria = createGPSFixDimensionFilterCriteria(selectionEntry.getKey(), selectionEntry.getValue());
            compoundFilterCriteria.addCriteria(criteria);
        }
        return compoundFilterCriteria;
    }
    
    public static <ValueType> FilterCriteria<GPSFixWithContext> createGPSFixDimensionFilterCriteria(SharedDimensions.GPSFix gpsFix, Collection<?> values) {
        Dimension<GPSFixWithContext, ValueType> dimension = Dimensions.GPSFix.getDimensionFor(gpsFix);
        return createDimensionFilterCriteria(dimension, values);
    }
    
    @SuppressWarnings("unchecked")
    public static <DataType, ValueType> FilterCriteria<DataType> createDimensionFilterCriteria(Dimension<DataType, ValueType> dimension, Collection<?> values) {
        return new DimensionValuesFilterCriteria<DataType, ValueType>(dimension, (Collection<ValueType>) values);
    }

    public static <ValueType> Grouper<GPSFixWithContext> createGPSFixByDimensionGrouper(SharedDimensions.GPSFix... dimensionTypes) {
        Collection<Dimension<GPSFixWithContext, ValueType>> dimensions = new HashSet<Dimension<GPSFixWithContext, ValueType>>();
        for (SharedDimensions.GPSFix dimensionType : dimensionTypes) {
            Dimension<GPSFixWithContext, ValueType> dimension = Dimensions.GPSFix.getDimensionFor(dimensionType);
            dimensions.add(dimension);
        }
        return new GroupGPSFixesByDimension<ValueType>(dimensions);
    }

    public static <DataType> Extractor<DataType, Integer> createDataAmountExtractor() {
        return new DataAmountExtractor<DataType>();
    }
    
    public static Aggregator<Integer, Integer> createIntegerAggregator(AggregatorType aggregatorType) {
        switch (aggregatorType) {
        case Average:
            return new IntegerAverageAggregator();
        case Sum:
            return new IntegerSumAggregator();
        }
        throw new IllegalArgumentException("Not yet implemented for the given aggregator type: "
                + aggregatorType.toString());
    }
    
    public static Aggregator<Double, Double> createDoubleAggregator(AggregatorType aggregatorType) {
        switch (aggregatorType) {
        case Average:
            return new DoubleAverageAggregator();
        case Sum:
            return new DoubleSumAggregator();
        }
        throw new IllegalArgumentException("Not yet implemented for the given aggregator type: "
                + aggregatorType.toString());
    }

}

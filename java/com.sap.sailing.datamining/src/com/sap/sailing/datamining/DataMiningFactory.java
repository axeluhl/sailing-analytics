package com.sap.sailing.datamining;

import java.util.Arrays;
import java.util.Collection;

import com.sap.sailing.datamining.impl.DataAmountExtractor;
import com.sap.sailing.datamining.impl.FilterByCriteriaImpl;
import com.sap.sailing.datamining.impl.QueryImpl;
import com.sap.sailing.datamining.impl.criterias.DimensionValuesFilterCriteria;
import com.sap.sailing.datamining.impl.gpsfix.GPSFixRetrieverImpl;
import com.sap.sailing.datamining.impl.gpsfix.GroupGPSFixesByDimension;
import com.sap.sailing.datamining.shared.AggregatorType;
import com.sap.sailing.datamining.shared.Dimensions.GPSFix;

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
    
    public static <ValueType> FilterCriteria<GPSFixWithContext> createGPSFixDimensionFilterCriteria(GPSFix dimensionType, Collection<ValueType> values) {
        Dimension<GPSFixWithContext, ValueType> dimension = Dimensions.GPSFix.getDimensionFor(dimensionType);
        return createDimensionFilterCriteria(dimension, values);
    }
    
    public static <DataType, ValueType> FilterCriteria<DataType> createDimensionFilterCriteria(Dimension<DataType, ValueType> dimension, Collection<ValueType> values) {
        return new DimensionValuesFilterCriteria<DataType, ValueType>(dimension, values);
    }

    public static <ValueType> Grouper<GPSFixWithContext> createGPSFixByDimensionGrouper(Dimension<GPSFixWithContext, ValueType>... dimensions) {
        Collection<Dimension<GPSFixWithContext, ValueType>> dimensionCollection = Arrays.asList(dimensions);
        return new GroupGPSFixesByDimension<ValueType>(dimensionCollection);
    }

    public static <DataType> Extractor<DataType, Integer> createDataAmountExtractor() {
        return new DataAmountExtractor<DataType>();
    }

    public static <ExtractedType, AggregatedType> Aggregator<ExtractedType, AggregatedType> createAggregator(
            AggregatorType aggregatorType) {
//        switch (aggregatorType) {
//        case Average:
//            return new AverageAggregator<ValueType, AveragesTo>();
//        case Sum:
//            return new SumAggregator<ValueType, AveragesTo>();
//
//        }
        throw new IllegalArgumentException("Not yet implemented for the given aggregator type: "
                + aggregatorType.toString());
    }
}

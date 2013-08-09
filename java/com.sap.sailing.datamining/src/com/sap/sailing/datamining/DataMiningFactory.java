package com.sap.sailing.datamining;

import java.util.Collection;

import com.sap.sailing.datamining.impl.AverageAggregator;
import com.sap.sailing.datamining.impl.DataAmountExtractor;
import com.sap.sailing.datamining.impl.FilterByCriteriaImpl;
import com.sap.sailing.datamining.impl.QueryImpl;
import com.sap.sailing.datamining.impl.SumAggregator;
import com.sap.sailing.datamining.impl.gpsfix.GPSFixRetrieverImpl;
import com.sap.sailing.datamining.impl.gpsfix.GroupGPSFixesByDimension;
import com.sap.sailing.datamining.shared.AggregatorType;

public class DataMiningFactory {
    
    private DataMiningFactory() { }

    public static <DataType, ValueType, AveragesTo> Query<DataType, ValueType, AveragesTo> createQuery(
            DataRetriever<DataType> retriever, Filter<DataType> filter, Grouper<DataType> grouper,
            Extractor<DataType, ValueType, AveragesTo> extractor, Aggregator<ValueType, AveragesTo> aggregator) {
        return new QueryImpl<DataType, ValueType, AveragesTo>(retriever, filter, grouper, extractor, aggregator);
    }
    
    /**
     * Creates a retriever for the given data type. Throws an exception, if the used <code>DataType</code> doesn't match the <code>DataType</code> of the returning retriever.
     */
    @SuppressWarnings("unchecked")
    public static <DataType> DataRetriever<DataType> createDataRetriever(com.sap.sailing.datamining.shared.DataType dataType) {
        switch (dataType) {
        case GPSFixes:
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

    public static Grouper<GPSFixWithContext> createGPSFixByDimensionGrouper(Dimension<GPSFixWithContext, String>... dimensions) {
        return new GroupGPSFixesByDimension(dimensions);
    }

    public static <DataType> Extractor<DataType, Integer, Integer> createDataSizeExtractor() {
        return new DataAmountExtractor<DataType>();
    }

    public static <ValueType, AveragesTo> Aggregator<ValueType, AveragesTo> createAggregator(
            AggregatorType aggregatorType) {
        switch (aggregatorType) {
        case Average:
            return new AverageAggregator<ValueType, AveragesTo>();
        case Sum:
            return new SumAggregator<ValueType, AveragesTo>();

        }
        throw new IllegalArgumentException("Not yet implemented for the given aggregator type: "
                + aggregatorType.toString());
    }
}

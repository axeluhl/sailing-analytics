package com.sap.sailing.datamining;

import com.sap.sailing.datamining.impl.AverageAggregator;
import com.sap.sailing.datamining.impl.DataSizeExtractor;
import com.sap.sailing.datamining.impl.FilterByCriteriaImpl;
import com.sap.sailing.datamining.impl.QueryImpl;
import com.sap.sailing.datamining.impl.SumAggregator;
import com.sap.sailing.datamining.impl.criterias.WildcardFilterCriteria;
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

    public static DataRetriever<GPSFixWithContext> createGPSFixRetriever() {
        return new GPSFixRetrieverImpl();
    }
    
    public static <DataType> FilterCriteria<DataType> createWildcardFilterCriteria() {
        return new WildcardFilterCriteria<DataType>();
    }

    public static <DataType> Filter<DataType> createCriteriaFilter(FilterCriteria<DataType> criteria) {
        return new FilterByCriteriaImpl<DataType>(criteria);
    }

    public static Grouper<GPSFixWithContext> createGPSFixByDimensionGrouper(Dimension<GPSFixWithContext, String> dimensions) {
        return new GroupGPSFixesByDimension(dimensions);
    }

    public static <DataType> Extractor<DataType, Integer, Integer> createDataSizeExtractor() {
        return new DataSizeExtractor<DataType>();
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

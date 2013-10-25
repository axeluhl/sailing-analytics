package com.sap.sailing.datamining;

import com.sap.sailing.datamining.factories.AggregatorFactory;
import com.sap.sailing.datamining.factories.DataRetrieverFactory;
import com.sap.sailing.datamining.factories.ExtractorFactory;
import com.sap.sailing.datamining.factories.FilterFactory;
import com.sap.sailing.datamining.factories.GrouperFactory;
import com.sap.sailing.datamining.impl.QueryImpl;
import com.sap.sailing.datamining.impl.SmartQueryDefinition;
import com.sap.sailing.datamining.shared.QueryDefinition;

public final class DataMiningFactory {
    
    private DataMiningFactory() { }

    public static <DataType, AggregatedType extends Number> Query<DataType, AggregatedType> createQuery(QueryDefinition queryDefinition) {
        SmartQueryDefinition smartQueryDefinition = new SmartQueryDefinition(queryDefinition);
        
        DataRetriever<DataType> retriever = DataRetrieverFactory.createDataRetriever(smartQueryDefinition.getDataType());
        Filter<DataType> filter = FilterFactory.createDimensionFilter(smartQueryDefinition.getDataType(), smartQueryDefinition.getSelection());
        Grouper<DataType> grouper = GrouperFactory.createGrouper(smartQueryDefinition);
        Extractor<DataType, AggregatedType> extractor = ExtractorFactory.createExtractor(smartQueryDefinition.getStatisticType());
        Aggregator<AggregatedType, AggregatedType> aggregator = AggregatorFactory.createAggregator(smartQueryDefinition.getStatisticType(), smartQueryDefinition.getAggregatorType());
        
        return new QueryImpl<DataType, AggregatedType, AggregatedType>(retriever, filter, grouper, extractor, aggregator);
    }

}

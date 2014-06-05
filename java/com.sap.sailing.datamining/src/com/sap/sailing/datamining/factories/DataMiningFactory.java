package com.sap.sailing.datamining.factories;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.datamining.DeprecatedToFunctionConverter;
import com.sap.sailing.datamining.QueryDefinitionConverter;
import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.QueryDefinitionDeprecated;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.factories.ProcessorFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.impl.ProcessorQuery;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public final class DataMiningFactory {
    
    private DataMiningFactory() {
    }
    
    // TODO Delete, after the deprecated components have been removed
    public static <DataSourceType, ElementType> Query<Double> createQuery(QueryDefinitionDeprecated queryDefinition, DataSourceType dataSource) {
        return createQuery(QueryDefinitionConverter.convertDeprecatedQueryDefinition(queryDefinition), dataSource);
    }

    // TODO Make public, after the deprecated components have been removed
    private static <DataSourceType, ElementType> Query<Double> createQuery(QueryDefinition queryDefinition, DataSourceType dataSource) {
        ProcessorQuery<Double, DataSourceType> query = ProcessorFactory.createProcessorQuery(dataSource, queryDefinition.getLocaleInfoName());
        Processor<GroupedDataEntry<Double>> aggregationProcessor = ProcessorFactory.createAggregationProcessor(query, queryDefinition.getAggregatorType());
        
        Function<Double> extractionFunction = getExtractionFunction(queryDefinition);
        Processor<GroupedDataEntry<ElementType>> extractionProcessor = ProcessorFactory.createExtractionProcessor(aggregationProcessor, extractionFunction);
        
        List<Function<?>> dimensionsToGroupBy = getDimensionsToGroupBy(queryDefinition);
        Processor<ElementType> groupingProcessor = ProcessorFactory.createGroupingProcessor(extractionProcessor, dimensionsToGroupBy);
        
        SailingDataRetrievalLevels dataRetrievalLevel = calculateDataRetrievalLevel(extractionFunction);
        Processor<DataSourceType> firstRetrievalProcessor = DataRetrieverFactory.createRetrievalProcessorChain(dataRetrievalLevel, groupingProcessor, queryDefinition.getFilterSelection());
        
        query.setFirstProcessor(firstRetrievalProcessor);
        return query;
    }

    //TODO Get the function from the function provider, not the converter
    @SuppressWarnings("unchecked") // All extraction functions in the DeprecatedToFunctionConverter have the return type Double
    private static Function<Double> getExtractionFunction(QueryDefinition queryDefinition) {
        return (Function<Double>) DeprecatedToFunctionConverter.getFunctionFor(queryDefinition.getExtractionFunction());
    }

    //TODO Get the functions from the function provider, not the converter
    private static List<Function<?>> getDimensionsToGroupBy(QueryDefinition queryDefinition) {
        List<Function<?>> dimensionsToGroupBy = new ArrayList<>();
        for (FunctionDTO functionDTO : queryDefinition.getDimensionsToGroupBy()) {
            dimensionsToGroupBy.add(DeprecatedToFunctionConverter.getFunctionFor(functionDTO));
        }
        return dimensionsToGroupBy;
    }
    
    //TODO Generalize the way to calculate the base data type from the extraction function
    private static SailingDataRetrievalLevels calculateDataRetrievalLevel(Function<Double> extractionFunction) {
        if (extractionFunction.getDeclaringType().equals(HasGPSFixContext.class)) {
            return SailingDataRetrievalLevels.GPSFix;
        }
        if (extractionFunction.getDeclaringType().equals(HasTrackedLegOfCompetitorContext.class)) {
            return SailingDataRetrievalLevels.TrackedLegOfCompetitor;
        }
        return SailingDataRetrievalLevels.GPSFix;
    }

}

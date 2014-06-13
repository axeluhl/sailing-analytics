package com.sap.sailing.datamining.factories;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.datamining.QueryDefinitionConverter;
import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.shared.QueryDefinitionDeprecated;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.factories.ProcessorFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.impl.ProcessorQuery;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.shared.QueryDefinition;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public final class DataMiningFactory {
    
    private DataMiningFactory() {
    }
    
    // TODO Delete, after the deprecated components have been removed
    public static <DataSourceType, ElementType> Query<Double> createQuery(QueryDefinitionDeprecated queryDefinition, DataSourceType dataSource, FunctionProvider functionProvider) {
        return createQuery(QueryDefinitionConverter.convertDeprecatedQueryDefinition(queryDefinition), dataSource, functionProvider);
    }

    // TODO Make public, after the deprecated components have been removed
    private static <DataSourceType, ElementType> Query<Double> createQuery(QueryDefinition queryDefinition, DataSourceType dataSource, FunctionProvider functionProvider) {
        ProcessorQuery<Double, DataSourceType> query = ProcessorFactory.createProcessorQuery(dataSource, queryDefinition.getLocaleInfoName());
        Processor<GroupedDataEntry<Double>> aggregationProcessor = ProcessorFactory.createAggregationProcessor(query, queryDefinition.getAggregatorType());
        
        @SuppressWarnings("unchecked") // TODO Clean, after the deprecated components have been removed
        Function<Double> extractionFunction = (Function<Double>) functionProvider.getFunctionForDTO(queryDefinition.getExtractionFunction());
        Processor<GroupedDataEntry<ElementType>> extractionProcessor = ProcessorFactory.createExtractionProcessor(aggregationProcessor, extractionFunction);
        
        List<Function<?>> dimensionsToGroupBy = getDimensionsToGroupBy(queryDefinition, functionProvider);
        Processor<ElementType> groupingProcessor = ProcessorFactory.createGroupingProcessor(extractionProcessor, dimensionsToGroupBy);
        
        SailingDataRetrievalLevels dataRetrievalLevel = calculateDataRetrievalLevel(extractionFunction);
        Processor<DataSourceType> firstRetrievalProcessor = SailingDataRetrieverFactory.createRetrievalProcessorChain(dataRetrievalLevel, groupingProcessor, queryDefinition.getFilterSelection(), functionProvider);
        
        query.setFirstProcessor(firstRetrievalProcessor);
        return query;
    }

    private static List<Function<?>> getDimensionsToGroupBy(QueryDefinition queryDefinition, FunctionProvider functionProvider) {
        List<Function<?>> dimensionsToGroupBy = new ArrayList<>();
        for (FunctionDTO functionDTO : queryDefinition.getDimensionsToGroupBy()) {
            dimensionsToGroupBy.add(functionProvider.getFunctionForDTO(functionDTO));
        }
        return dimensionsToGroupBy;
    }
    
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

package com.sap.sailing.datamining.factories;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.datamining.QueryDefinitionConverter;
import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.datamining.shared.QueryDefinitionDeprecated;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.factories.ProcessorFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.impl.DataMiningActivator;
import com.sap.sse.datamining.impl.ProcessorQuery;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.shared.QueryDefinition;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public final class DataMiningFactory {
    
    private DataMiningFactory() {
    }
    
    // TODO Delete, after the deprecated components have been removed
    public static <DataSourceType, ElementType> Query<Double> createQuery(DataSourceType dataSource, QueryDefinitionDeprecated queryDefinition, FunctionProvider functionProvider) {
        return createQuery(dataSource, QueryDefinitionConverter.convertDeprecatedQueryDefinition(queryDefinition), functionProvider);
    }

    // TODO Make public, after the deprecated components have been removed
    private static <DataSourceType, ElementType> Query<Double> createQuery(DataSourceType dataSource, final QueryDefinition queryDefinition, final FunctionProvider functionProvider) {
        return new ProcessorQuery<Double, DataSourceType>(DataMiningActivator.getExecutor(), dataSource,
                DataMiningActivator.getStringMessages(), DataMiningStringMessages.Util.getLocaleFor(queryDefinition.getLocaleInfoName())) {
            
            @Override
            protected Processor<DataSourceType> createFirstProcessor() {
                Processor<GroupedDataEntry<Double>> aggregationProcessor = ProcessorFactory.createAggregationProcessor(/*query*/ this, queryDefinition.getAggregatorType());
                
                @SuppressWarnings("unchecked") // TODO Clean, after the deprecated components have been removed
                Function<Double> extractionFunction = (Function<Double>) functionProvider.getFunctionForDTO(queryDefinition.getExtractionFunction());
                Processor<GroupedDataEntry<ElementType>> extractionProcessor = ProcessorFactory.createExtractionProcessor(aggregationProcessor, extractionFunction);
                
                List<Function<?>> dimensionsToGroupBy = getDimensionsToGroupBy(queryDefinition, functionProvider);
                Processor<ElementType> groupingProcessor = ProcessorFactory.createGroupingProcessor(extractionProcessor, dimensionsToGroupBy);
                
                SailingDataRetrievalLevels dataRetrievalLevel = calculateDataRetrievalLevel(extractionFunction);
                Processor<DataSourceType> firstRetrievalProcessor = SailingDataRetrieverFactory.createRetrievalProcessorChain(dataRetrievalLevel, groupingProcessor, queryDefinition.getFilterSelection(), functionProvider);
                return firstRetrievalProcessor;
            }
            
        };
    }

    private static List<Function<?>> getDimensionsToGroupBy(QueryDefinition queryDefinition, FunctionProvider functionProvider) {
        List<Function<?>> dimensionsToGroupBy = new ArrayList<>();
        for (FunctionDTO functionDTO : queryDefinition.getDimensionsToGroupBy()) {
            dimensionsToGroupBy.add(functionProvider.getFunctionForDTO(functionDTO));
        }
        return dimensionsToGroupBy;
    }
    
    private static SailingDataRetrievalLevels calculateDataRetrievalLevel(Function<Double> extractionFunction) {
        if (extractionFunction.getDeclaringType().equals(HasTrackedRaceContext.class)) {
            return SailingDataRetrievalLevels.TrackedRace;
        }
        if (extractionFunction.getDeclaringType().equals(HasTrackedLegContext.class)) {
            return SailingDataRetrievalLevels.TrackedLeg;
        }
        if (extractionFunction.getDeclaringType().equals(HasTrackedLegOfCompetitorContext.class)) {
            return SailingDataRetrievalLevels.TrackedLegOfCompetitor;
        }
        if (extractionFunction.getDeclaringType().equals(HasGPSFixContext.class)) {
            return SailingDataRetrievalLevels.GPSFix;
        }
        return SailingDataRetrievalLevels.GPSFix;
    }

}

package com.sap.sailing.datamining.factories;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.server.RacingEventService;
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

public final class SailingDataMiningFactory {
    
    private SailingDataMiningFactory() {
    }

    public static <ElementType> Query<Double> createQuery(RacingEventService dataSource, final QueryDefinition queryDefinition, final FunctionProvider functionProvider) {
        return new ProcessorQuery<Double, RacingEventService>(DataMiningActivator.getExecutor(), dataSource,
                DataMiningActivator.getStringMessages(), DataMiningStringMessages.Util.getLocaleFor(queryDefinition.getLocaleInfoName())) {
            
            @Override
            protected Processor<RacingEventService> createFirstProcessor() {
                Processor<GroupedDataEntry<Double>> aggregationProcessor = ProcessorFactory.createAggregationProcessor(/*query*/ this, queryDefinition.getAggregatorType());
                
                @SuppressWarnings("unchecked") // TODO Clean, after the deprecated components have been removed
                Function<Double> extractionFunction = (Function<Double>) functionProvider.getFunctionForDTO(queryDefinition.getStatisticToCalculate());
                Processor<GroupedDataEntry<ElementType>> extractionProcessor = ProcessorFactory.createExtractionProcessor(aggregationProcessor, extractionFunction);
                
                List<Function<?>> dimensionsToGroupBy = convertDTOsToFunctions(queryDefinition.getDimensionsToGroupBy(), functionProvider);
                @SuppressWarnings("unchecked")
                Processor<ElementType> groupingProcessor = ProcessorFactory.createGroupingProcessor((Class<ElementType>) extractionFunction.getDeclaringType(), extractionProcessor, dimensionsToGroupBy);
                
                SailingDataRetrievalLevels dataRetrievalLevel = calculateDataRetrievalLevel(extractionFunction);
                Processor<RacingEventService> firstRetrievalProcessor = SailingDataRetrieverFactory.createRetrievalProcessorChain(dataRetrievalLevel, groupingProcessor, queryDefinition.getFilterSelection(), functionProvider);
                return firstRetrievalProcessor;
            }
            
        };
    }

    @SuppressWarnings("unchecked")
    public static Query<Set<Object>> createDimensionValuesQuery(RacingEventService dataSource, final Collection<FunctionDTO> dimensionDTOs, final FunctionProvider functionProvider) {
        return new ProcessorQuery<Set<Object>, RacingEventService>(DataMiningActivator.getExecutor(), dataSource) {
            @Override
            protected Processor<RacingEventService> createFirstProcessor() {
                Processor<GroupedDataEntry<Object>> valueCollector = ProcessorFactory.createGroupedDataCollectingAsSetProcessor(/*query*/ this);
                
                Collection<Function<?>> dimensions = convertDTOsToFunctions(dimensionDTOs, functionProvider);
                Map<SailingDataRetrievalLevels, Collection<Function<?>>> dimensionsMappedByRetrievalLevel = mapFunctionsByRetrievalLevel(dimensions);
                SailingDataRetrievalLevels deepestRetrievalLevel = getDeepestRetrievalLevel(dimensionsMappedByRetrievalLevel.keySet());

                Processor<?> retrievalProcessor = null;
                for (int i = deepestRetrievalLevel.ordinal(); i >= 0; i--) {
                    SailingDataRetrievalLevels retrievalLevel = SailingDataRetrievalLevels.values()[i];
                    
                    Class<?> dataType = retrievalLevel.getDataType();
                    Collection<Function<?>> dimensionsForRetrievalLevel = dimensionsMappedByRetrievalLevel.containsKey(retrievalLevel) ? dimensionsMappedByRetrievalLevel.get(retrievalLevel) : new ArrayList<Function<?>>();
                    Collection<Processor<?>> retrievalResultReceivers = ProcessorFactory.createGroupingExtractorsForDimensions(dataType, valueCollector, dimensionsForRetrievalLevel);
                    if (retrievalProcessor != null) {
                        retrievalResultReceivers.add(retrievalProcessor);
                    }
                    
                    retrievalProcessor = SailingDataRetrieverFactory.createRetrievalProcessorWithoutFilter(retrievalLevel, retrievalResultReceivers);
                }
                
                // A retrieval processor created this way results always in a Processor<RacingEventService>
                return (Processor<RacingEventService>) retrievalProcessor;
            }
        };
    }

    private static List<Function<?>> convertDTOsToFunctions(Collection<FunctionDTO> functionDTOs, FunctionProvider functionProvider) {
        List<Function<?>> dimensionsToGroupBy = new ArrayList<>();
        for (FunctionDTO functionDTO : functionDTOs) {
            dimensionsToGroupBy.add(functionProvider.getFunctionForDTO(functionDTO));
        }
        return dimensionsToGroupBy;
    }
    
    private static Map<SailingDataRetrievalLevels, Collection<Function<?>>> mapFunctionsByRetrievalLevel(Collection<Function<?>> functions) {
        Map<SailingDataRetrievalLevels, Collection<Function<?>>> mappedFunctions = new HashMap<>();
        for (Function<?> function : functions) {
            SailingDataRetrievalLevels retrievalLevel = calculateDataRetrievalLevel(function);
            if (!mappedFunctions.containsKey(retrievalLevel)) {
                mappedFunctions.put(retrievalLevel, new HashSet<Function<?>>());
            }
            mappedFunctions.get(retrievalLevel).add(function);
        }
        return mappedFunctions;
    }
    
    private static SailingDataRetrievalLevels calculateDataRetrievalLevel(Function<?> function) {
        if (function.getDeclaringType().equals(HasTrackedRaceContext.class)) {
            return SailingDataRetrievalLevels.TrackedRace;
        }
        if (function.getDeclaringType().equals(HasTrackedLegContext.class)) {
            return SailingDataRetrievalLevels.TrackedLeg;
        }
        if (function.getDeclaringType().equals(HasTrackedLegOfCompetitorContext.class)) {
            return SailingDataRetrievalLevels.TrackedLegOfCompetitor;
        }
        if (function.getDeclaringType().equals(HasGPSFixContext.class)) {
            return SailingDataRetrievalLevels.GPSFix;
        }
        return SailingDataRetrievalLevels.GPSFix;
    }

    private static SailingDataRetrievalLevels getDeepestRetrievalLevel(Collection<SailingDataRetrievalLevels> retrievalLevels) {
        return Collections.max(retrievalLevels);
    }

}

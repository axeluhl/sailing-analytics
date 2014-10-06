package com.sap.sailing.datamining.factories;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.datamining.impl.components.GPSFixRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.LeaderboardGroupRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.RegattaLeaderboardFilteringRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.TrackedLegFilteringRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.TrackedLegOfCompetitorFilteringRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.TrackedRaceFilteringRetrievalProcessor;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.DataRetrieverChainBuilder;
import com.sap.sse.datamining.DataRetrieverChainDefinition;
import com.sap.sse.datamining.Query;
import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.factories.ProcessorFactory;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.i18n.DataMiningStringMessages;
import com.sap.sse.datamining.impl.DataMiningActivator;
import com.sap.sse.datamining.impl.ProcessorQuery;
import com.sap.sse.datamining.impl.SimpleDataRetrieverChainDefinition;
import com.sap.sse.datamining.impl.components.GroupedDataEntry;
import com.sap.sse.datamining.impl.criterias.AndCompoundFilterCriterion;
import com.sap.sse.datamining.impl.criterias.CompoundFilterCriterion;
import com.sap.sse.datamining.impl.criterias.NullaryFunctionValuesFilterCriterion;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryDefinition;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public class SailingDataMiningFactory {
    
    // TODO Remove after the DataRetrieverChainDefinitionProvider has been implemented
    private final DataRetrieverChainDefinition<RacingEventService> dataRetrieverChainDefinition;
    
    public SailingDataMiningFactory() {
        dataRetrieverChainDefinition = new SimpleDataRetrieverChainDefinition<>(RacingEventService.class);
        
        @SuppressWarnings("unchecked")
        Class<Processor<RacingEventService, LeaderboardGroup>> leaderboardGroupRetrieverType = (Class<Processor<RacingEventService, LeaderboardGroup>>)(Class<?>) LeaderboardGroupRetrievalProcessor.class;
        dataRetrieverChainDefinition.startWith(leaderboardGroupRetrieverType, LeaderboardGroup.class);
        
        @SuppressWarnings("unchecked")
        Class<Processor<LeaderboardGroup, RegattaLeaderboard>> regattaLeaderboardRetrieverType = (Class<Processor<LeaderboardGroup, RegattaLeaderboard>>)(Class<?>) RegattaLeaderboardFilteringRetrievalProcessor.class;
        dataRetrieverChainDefinition.addAsLast(leaderboardGroupRetrieverType, regattaLeaderboardRetrieverType, RegattaLeaderboard.class);

        @SuppressWarnings("unchecked")
        Class<Processor<RegattaLeaderboard, HasTrackedRaceContext>> raceRetrieverType = (Class<Processor<RegattaLeaderboard, HasTrackedRaceContext>>)(Class<?>) TrackedRaceFilteringRetrievalProcessor.class;
        dataRetrieverChainDefinition.addAsLast(regattaLeaderboardRetrieverType, raceRetrieverType, HasTrackedRaceContext.class);

        @SuppressWarnings("unchecked")
        Class<Processor<HasTrackedRaceContext, HasTrackedLegContext>> legRetrieverType = (Class<Processor<HasTrackedRaceContext, HasTrackedLegContext>>)(Class<?>) TrackedLegFilteringRetrievalProcessor.class;
        dataRetrieverChainDefinition.addAsLast(raceRetrieverType, legRetrieverType, HasTrackedLegContext.class);

        @SuppressWarnings("unchecked")
        Class<Processor<HasTrackedLegContext, HasTrackedLegOfCompetitorContext>> legOfCompetitorRetrieverType = (Class<Processor<HasTrackedLegContext, HasTrackedLegOfCompetitorContext>>)(Class<?>) TrackedLegOfCompetitorFilteringRetrievalProcessor.class;
        dataRetrieverChainDefinition.addAsLast(legRetrieverType, legOfCompetitorRetrieverType, HasTrackedLegOfCompetitorContext.class);

        @SuppressWarnings("unchecked")
        Class<Processor<HasTrackedLegOfCompetitorContext, HasGPSFixContext>> gpsFixRetrieverType = (Class<Processor<HasTrackedLegOfCompetitorContext, HasGPSFixContext>>)(Class<?>) GPSFixRetrievalProcessor.class;
        dataRetrieverChainDefinition.addAsLast(legOfCompetitorRetrieverType, gpsFixRetrieverType, HasGPSFixContext.class);
    }

    public <ElementType> Query<Double> createQuery(RacingEventService dataSource, final QueryDefinition queryDefinition, final FunctionProvider functionProvider) {
        return new ProcessorQuery<Double, RacingEventService>(DataMiningActivator.getExecutor(), dataSource,
                DataMiningActivator.getStringMessages(), DataMiningStringMessages.Util.getLocaleFor(queryDefinition.getLocaleInfoName())) {
            @Override
            protected Processor<RacingEventService, ?> createFirstProcessor() {
                Processor<GroupedDataEntry<Double>, Map<GroupKey, Double>> aggregationProcessor = ProcessorFactory.createAggregationProcessor(/*query*/ this, queryDefinition.getAggregatorType());
                
                @SuppressWarnings("unchecked") // TODO Clean, after the deprecated components have been removed
                Function<Double> extractionFunction = (Function<Double>) functionProvider.getFunctionForDTO(queryDefinition.getStatisticToCalculate());
                Processor<GroupedDataEntry<ElementType>, GroupedDataEntry<Double>> extractionProcessor = ProcessorFactory.createExtractionProcessor(aggregationProcessor, extractionFunction);
                
                List<Function<?>> dimensionsToGroupBy = convertDTOsToFunctions(queryDefinition.getDimensionsToGroupBy(), functionProvider);
                @SuppressWarnings("unchecked")
                Processor<ElementType, GroupedDataEntry<ElementType>> groupingProcessor = ProcessorFactory.createGroupingProcessor((Class<ElementType>) extractionFunction.getDeclaringType(), extractionProcessor, dimensionsToGroupBy);

                DataRetrieverChainBuilder<RacingEventService> chainBuilder = dataRetrieverChainDefinition.startBuilding(DataMiningActivator.getExecutor());
                Map<Class<?>, FilterCriterion<?>> criteriaMappedByDataType = createFilterCriteria(queryDefinition.getFilterSelection(), functionProvider);
                Class<?> dataTypeToRetrieve = extractionFunction.getDeclaringType();
                do {
                    if (criteriaMappedByDataType.containsKey(chainBuilder.getCurrentRetrievedDataType())) {
                        chainBuilder.setFilter(criteriaMappedByDataType.get(chainBuilder.getCurrentRetrievedDataType()));
                    }
                    
                    chainBuilder.stepDeeper();
                } while (!dataTypeToRetrieve.equals(chainBuilder.getCurrentRetrievedDataType()));
                if (criteriaMappedByDataType.containsKey(chainBuilder.getCurrentRetrievedDataType())) {
                    chainBuilder.setFilter(criteriaMappedByDataType.get(chainBuilder.getCurrentRetrievedDataType()));
                }
                chainBuilder.addResultReceiver(groupingProcessor);
                
                return chainBuilder.build();
            }
            
        };
    }
    
    @SuppressWarnings("unchecked")
    private <T> Map<Class<?>, FilterCriterion<?>> createFilterCriteria(Map<FunctionDTO, Iterable<? extends Serializable>> filterSelection, FunctionProvider functionProvider) {
        Map<Class<?>, CompoundFilterCriterion<?>> criteriaMappedByDataType = new HashMap<>();
        for (Entry<FunctionDTO, Iterable<? extends Serializable>> filterSelectionEntry : filterSelection.entrySet()) {
            Function<?> function = functionProvider.getFunctionForDTO(filterSelectionEntry.getKey());
            Class<T> dataType = (Class<T>) function.getDeclaringType();
            
            if (!criteriaMappedByDataType.containsKey(dataType)) {
                criteriaMappedByDataType.put(dataType, new AndCompoundFilterCriterion<>(dataType));
            }

            Collection<Object> filterValues = new ArrayList<>();
            for (Object filterValue : filterSelectionEntry.getValue()) {
                filterValues.add(filterValue);
            }
            ((CompoundFilterCriterion<T>) criteriaMappedByDataType.get(dataType)).addCriteria(new NullaryFunctionValuesFilterCriterion<>(dataType, function, filterValues));
        }
        return (Map<Class<?>, FilterCriterion<?>>)(Map<Class<?>, ?>) criteriaMappedByDataType;
    }

    public Query<Set<Object>> createDimensionValuesQuery(RacingEventService dataSource, final Collection<FunctionDTO> dimensionDTOs, final FunctionProvider functionProvider) {
        return new ProcessorQuery<Set<Object>, RacingEventService>(DataMiningActivator.getExecutor(), dataSource) {
            @Override
            protected Processor<RacingEventService, ?> createFirstProcessor() {
                Processor<GroupedDataEntry<Object>, Map<GroupKey, Set<Object>>> valueCollector = ProcessorFactory.createGroupedDataCollectingAsSetProcessor(/*query*/ this);
                
                DataRetrieverChainBuilder<RacingEventService> chainBuilder = dataRetrieverChainDefinition.startBuilding(DataMiningActivator.getExecutor());
                Collection<Function<?>> dimensions = convertDTOsToFunctions(dimensionDTOs, functionProvider);
                Map<Class<?>, Collection<Function<?>>> dimensionsMappedByDeclaringType = mapFunctionsByDeclaringType(dimensions);
                while (!dimensionsMappedByDeclaringType.isEmpty()) {
                    Class<?> dataType = chainBuilder.getCurrentRetrievedDataType();
                    
                    if (dimensionsMappedByDeclaringType.containsKey(dataType)) {
                        for (Processor<?, ?> resultReceiver : ProcessorFactory.createGroupingExtractorsForDimensions(
                                                                dataType, valueCollector, dimensionsMappedByDeclaringType.get(dataType))) {
                            chainBuilder.addResultReceiver(resultReceiver);
                        }
                        dimensionsMappedByDeclaringType.remove(dataType);
                    }
                    
                    if (!dimensionsMappedByDeclaringType.isEmpty()) {
                        chainBuilder.stepDeeper();
                    }
                }
                
                return chainBuilder.build();
            }
        };
    }

    private List<Function<?>> convertDTOsToFunctions(Collection<FunctionDTO> functionDTOs, FunctionProvider functionProvider) {
        List<Function<?>> dimensionsToGroupBy = new ArrayList<>();
        for (FunctionDTO functionDTO : functionDTOs) {
            dimensionsToGroupBy.add(functionProvider.getFunctionForDTO(functionDTO));
        }
        return dimensionsToGroupBy;
    }
    
    private Map<Class<?>, Collection<Function<?>>> mapFunctionsByDeclaringType(Collection<Function<?>> functions) {
        Map<Class<?>, Collection<Function<?>>> mappedFunctions = new HashMap<>();
        for (Function<?> function : functions) {
            Class<?> declaringType = function.getDeclaringType();
            if (!mappedFunctions.containsKey(declaringType)) {
                mappedFunctions.put(declaringType, new HashSet<Function<?>>());
            }
            mappedFunctions.get(declaringType).add(function);
        }
        return mappedFunctions;
    }

}

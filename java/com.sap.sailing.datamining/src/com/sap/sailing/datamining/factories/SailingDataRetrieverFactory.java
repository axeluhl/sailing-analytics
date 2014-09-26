package com.sap.sailing.datamining.factories;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadPoolExecutor;

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
import com.sap.sse.datamining.components.FilterCriterion;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.functions.Function;
import com.sap.sse.datamining.functions.FunctionProvider;
import com.sap.sse.datamining.impl.DataMiningActivator;
import com.sap.sse.datamining.impl.criterias.AndCompoundFilterCriterion;
import com.sap.sse.datamining.impl.criterias.CompoundFilterCriterion;
import com.sap.sse.datamining.impl.criterias.NonFilteringFilterCriterion;
import com.sap.sse.datamining.impl.criterias.NullaryFunctionValuesFilterCriterion;
import com.sap.sse.datamining.shared.dto.FunctionDTO;

public final class SailingDataRetrieverFactory {

    private SailingDataRetrieverFactory() {
    }
    
    /**
     * Creates the processor chain for the data retrieval and returns the first processor of the chain.
     * @param groupingProcessor The result receiver after the retrieval
     * @param filterSelection The filter functions and values for the filtration
     * @param functionProvider 
     * @return The first processor of the retrieval chain.
     */
    @SuppressWarnings("unchecked")
    public static <ElementType> Processor<RacingEventService> createRetrievalProcessorChain(SailingDataRetrievalLevels dataRetrievalLevel, Processor<ElementType> groupingProcessor, Map<FunctionDTO, Iterable<? extends Serializable>> filterSelection, FunctionProvider functionProvider) {
        Processor<?> resultReceiver = groupingProcessor;
        for (int i = dataRetrievalLevel.ordinal(); i >= 0; i--) {
            resultReceiver = createDataRetrieverFor(SailingDataRetrievalLevels.values()[i], resultReceiver, filterSelection, functionProvider);
        }
        return (Processor<RacingEventService>) resultReceiver;
    }

    @SuppressWarnings("unchecked")
    /* The way this method is used should guarantee, that the result receiver matches the new processor 
     */
    private static Processor<?> createDataRetrieverFor(SailingDataRetrievalLevels dataRetrievalLevel,
            Processor<?> resultReceiver, Map<FunctionDTO, Iterable<? extends Serializable>> filterSelection, FunctionProvider functionProvider) {
        ThreadPoolExecutor executor = DataMiningActivator.getExecutor();
        
        switch (dataRetrievalLevel) {
        case GPSFix:
            Processor<HasGPSFixContext> gpsFixSpecificResultReceiver = (Processor<HasGPSFixContext>) resultReceiver;
            return new GPSFixRetrievalProcessor(executor, Arrays.asList(gpsFixSpecificResultReceiver));
        case LeaderboardGroup:
            Processor<LeaderboardGroup> leaderboardGroupSpecificResultReceiver = (Processor<LeaderboardGroup>) resultReceiver;
            return new LeaderboardGroupRetrievalProcessor(executor, Arrays.asList(leaderboardGroupSpecificResultReceiver));
        case RegattaLeaderboard:
            Processor<RegattaLeaderboard> regattaLeaderboardSpecificResultReceiver = (Processor<RegattaLeaderboard>) resultReceiver;
            return new RegattaLeaderboardFilteringRetrievalProcessor(executor, Arrays.asList(regattaLeaderboardSpecificResultReceiver), getFilterCriteriaForBaseDataType(RegattaLeaderboard.class, filterSelection, functionProvider));
        case TrackedLeg:
            Processor<HasTrackedLegContext> trackedLegSpecificResultReceiver = (Processor<HasTrackedLegContext>) resultReceiver;
            return new TrackedLegFilteringRetrievalProcessor(executor, Arrays.asList(trackedLegSpecificResultReceiver), getFilterCriteriaForBaseDataType(HasTrackedLegContext.class, filterSelection, functionProvider));
        case TrackedLegOfCompetitor:
            Processor<HasTrackedLegOfCompetitorContext> trackedLegOfCompetitorSpecificResultReceiver = (Processor<HasTrackedLegOfCompetitorContext>) resultReceiver;
            return new TrackedLegOfCompetitorFilteringRetrievalProcessor(executor, Arrays.asList(trackedLegOfCompetitorSpecificResultReceiver), getFilterCriteriaForBaseDataType(HasTrackedLegOfCompetitorContext.class, filterSelection, functionProvider));
        case TrackedRace:
            Processor<HasTrackedRaceContext> trackedRaceSpecificResultReceiver = (Processor<HasTrackedRaceContext>) resultReceiver;
            return new TrackedRaceFilteringRetrievalProcessor(executor, Arrays.asList(trackedRaceSpecificResultReceiver), getFilterCriteriaForBaseDataType(HasTrackedRaceContext.class, filterSelection, functionProvider));
        }
        throw new IllegalArgumentException("No data retriever implemented for the given data retrieval level '"
                + dataRetrievalLevel + "'");
    }

    private static <BaseDataType> FilterCriterion<BaseDataType> getFilterCriteriaForBaseDataType(Class<BaseDataType> baseDataType,
            Map<FunctionDTO, Iterable<? extends Serializable>> filterSelection, FunctionProvider functionProvider) {
        CompoundFilterCriterion<BaseDataType> criteria = null;
        for (Entry<FunctionDTO, Iterable<? extends Serializable>> filterSelectionEntry : filterSelection.entrySet()) {
            Function<?> function = functionProvider.getFunctionForDTO(filterSelectionEntry.getKey());
            if (baseDataType.equals(function.getDeclaringType())) {
                if (criteria == null) {
                    criteria = new AndCompoundFilterCriterion<>();
                }
                
                Collection<Object> filterValues = new ArrayList<>();
                for (Object filterValue : filterSelectionEntry.getValue()) {
                    filterValues.add(filterValue);
                }
                criteria.addCriteria(new NullaryFunctionValuesFilterCriterion<BaseDataType>(function, filterValues));
            }
        }
        return criteria != null ? criteria : new NonFilteringFilterCriterion<BaseDataType>();
    }

    /**
     * Creates a retrieval processor without filter for the given retrieval level and result receivers.<br>
     * The input type of the result receivers has to match the result type of the created retrieval processor for the given retrievel level or a {@link ClassCastException} can be thrown.
     * For example, if the retrieval Level is {@link SailingDataRetrievalLevels#TrackedRace} the input must be {@link HasTrackedRaceContext}.
     */
    @SuppressWarnings("unchecked")
    public static Processor<?> createRetrievalProcessorWithoutFilter(SailingDataRetrievalLevels retrievalLevel,
            Collection<Processor<?>> retrievalResultReceivers) throws ClassCastException {
        ThreadPoolExecutor executor = DataMiningActivator.getExecutor();
        
        switch (retrievalLevel) {
        case GPSFix:
            Collection<Processor<HasGPSFixContext>> gpsFixSpecificResultReceivers = (Collection<Processor<HasGPSFixContext>>)(Collection<?>) retrievalResultReceivers;
            return new GPSFixRetrievalProcessor(executor, gpsFixSpecificResultReceivers);
        case LeaderboardGroup:
            Collection<Processor<LeaderboardGroup>> leaderboardGroupSpecificResultReceivers = (Collection<Processor<LeaderboardGroup>>)(Collection<?>) retrievalResultReceivers;
            return new LeaderboardGroupRetrievalProcessor(executor, leaderboardGroupSpecificResultReceivers);
        case RegattaLeaderboard:
            Collection<Processor<RegattaLeaderboard>> regattaLeaderboardSpecificResultReceivers = (Collection<Processor<RegattaLeaderboard>>)(Collection<?>) retrievalResultReceivers;
            return new RegattaLeaderboardFilteringRetrievalProcessor(executor, regattaLeaderboardSpecificResultReceivers, new NonFilteringFilterCriterion<RegattaLeaderboard>());
        case TrackedLeg:
            Collection<Processor<HasTrackedLegContext>> trackedLegSpecificResultReceivers = (Collection<Processor<HasTrackedLegContext>>)(Collection<?>) retrievalResultReceivers;
            return new TrackedLegFilteringRetrievalProcessor(executor, trackedLegSpecificResultReceivers, new NonFilteringFilterCriterion<HasTrackedLegContext>());
        case TrackedLegOfCompetitor:
            Collection<Processor<HasTrackedLegOfCompetitorContext>> trackedLegOfCompetitorSpecificResultReceivers = (Collection<Processor<HasTrackedLegOfCompetitorContext>>)(Collection<?>) retrievalResultReceivers;
            return new TrackedLegOfCompetitorFilteringRetrievalProcessor(executor, trackedLegOfCompetitorSpecificResultReceivers, new NonFilteringFilterCriterion<HasTrackedLegOfCompetitorContext>());
        case TrackedRace:
            Collection<Processor<HasTrackedRaceContext>> trackedRaceSpecificResultReceivers = (Collection<Processor<HasTrackedRaceContext>>)(Collection<?>) retrievalResultReceivers;
            return new TrackedRaceFilteringRetrievalProcessor(executor, trackedRaceSpecificResultReceivers, new NonFilteringFilterCriterion<HasTrackedRaceContext>());
        }
        throw new IllegalArgumentException("No data retriever implemented for the given data retrieval level '"
                + retrievalLevel + "'");
    }

}

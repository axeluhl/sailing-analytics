package com.sap.sailing.datamining.factories;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.impl.components.GPSFixRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.LeaderboardGroupRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.RegattaLeaderboardFilteringRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.TrackedLegFilteringRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.TrackedLegOfCompetitorFilteringRetrievalProcessor;
import com.sap.sailing.datamining.impl.components.deprecated.GroupDividingParallelDataRetriever;
import com.sap.sailing.datamining.impl.workers.builders.deprecated.DataRetrieverWorkerBuilder;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.components.ParallelDataRetriever;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.DataMiningActivator;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.datamining.workers.DataRetrievalWorker;
import com.sap.sse.datamining.workers.WorkerBuilder;

public final class DataRetrieverFactory {

    private DataRetrieverFactory() {
    }
    
    /**
     * Creates the processor chain for the data retrieval and returns the first processor of the chain.
     * @param groupingProcessor The result receiver after the retrieval
     * @param filterSelection The filter functions and values for the filtration
     * @return The first processor of the retrieval chain.
     */
    @SuppressWarnings("unchecked")
    public static <DataSourceType, ElementType> Processor<DataSourceType> createRetrievalProcessorChain(SailingDataRetrievalLevels dataRetrievalLevel, Processor<ElementType> groupingProcessor, Map<FunctionDTO, Iterable<?>> filterSelection) {
        Processor<?> resultReceiver = groupingProcessor;
        for (int i = dataRetrievalLevel.ordinal(); i <= 0; i--) {
            resultReceiver = createDataRetrieverFor(SailingDataRetrievalLevels.values()[i], resultReceiver, filterSelection);
        }
        return (Processor<DataSourceType>) resultReceiver;
    }

    @SuppressWarnings("unchecked")
    /* The way this method is used should guarantee, that the result receiver matches the new processor 
     */
    private static Processor<?> createDataRetrieverFor(SailingDataRetrievalLevels dataRetrievalLevel,
            Processor<?> resultReceiver, Map<FunctionDTO, Iterable<?>> filterSelection) {
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
            return new RegattaLeaderboardFilteringRetrievalProcessor(executor, Arrays.asList(regattaLeaderboardSpecificResultReceiver), getFilterCriteriaForBaseDataType(RegattaLeaderboard.class, filterSelection));
        case TrackedLeg:
            Processor<HasTrackedLegContext> trackedLegSpecificResultReceiver = (Processor<HasTrackedLegContext>) resultReceiver;
            return new TrackedLegFilteringRetrievalProcessor(executor, Arrays.asList(trackedLegSpecificResultReceiver), getFilterCriteriaForBaseDataType(HasTrackedLegContext.class, filterSelection));
        case TrackedLegOfCompetitor:
            Processor<HasTrackedLegOfCompetitorContext> trackedLegOfCompetitorSpecificResultReceiver = (Processor<HasTrackedLegOfCompetitorContext>) resultReceiver;
            return new TrackedLegOfCompetitorFilteringRetrievalProcessor(executor, Arrays.asList(trackedLegOfCompetitorSpecificResultReceiver), getFilterCriteriaForBaseDataType(HasTrackedLegOfCompetitorContext.class, filterSelection));
        }
        throw new IllegalArgumentException("No data retriever implemented for the given data retrieval level '"
                + dataRetrievalLevel + "'");
    }

    private static <BaseDataType> FilterCriteria<BaseDataType> getFilterCriteriaForBaseDataType(Class<BaseDataType> baseDataType,
            Map<FunctionDTO, Iterable<?>> filterSelection) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Creates a retriever for the given data type. Throws an exception, if the used <code>DataType</code> doesn't match
     * the <code>DataType</code> of the returning retriever.
     */
    public static <DataType> ParallelDataRetriever<DataType> createDataRetriever(DataTypes dataType, RacingEventService racingService, ThreadPoolExecutor executor) {
        WorkerBuilder<DataRetrievalWorker<LeaderboardGroup, DataType>> workerBuilder = new DataRetrieverWorkerBuilder<DataType>(dataType);
        return new GroupDividingParallelDataRetriever<DataType>(racingService, workerBuilder, executor);
    }

}

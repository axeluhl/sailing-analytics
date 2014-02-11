package com.sap.sailing.datamining.factories;

import java.util.concurrent.ThreadPoolExecutor;

import com.sap.sailing.datamining.builders.DataRetrieverWorkerBuilder;
import com.sap.sailing.datamining.impl.GroupDividingParallelDataRetriever;
import com.sap.sailing.datamining.shared.DataTypes;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.components.ParallelDataRetriever;
import com.sap.sse.datamining.workers.DataRetrievalWorker;
import com.sap.sse.datamining.workers.WorkerBuilder;

public final class DataRetrieverFactory {

    private DataRetrieverFactory() {
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

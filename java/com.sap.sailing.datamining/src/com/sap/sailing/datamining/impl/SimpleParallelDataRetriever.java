package com.sap.sailing.datamining.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;

import com.sap.sailing.datamining.DataRetrievalWorker;
import com.sap.sailing.datamining.ParallelDataRetriever;
import com.sap.sailing.datamining.WorkerBuilder;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.RacingEventService;

public class SimpleParallelDataRetriever<DataType> extends AbstractParallelComponent<RacingEventService, Collection<DataType>>
                                                   implements ParallelDataRetriever<DataType> {

    private WorkerBuilder<DataRetrievalWorker<DataType>> workerBuilder;

    /**
     * Creates a new parallel working data retriever with the given executer and data retriever as base for the workers.
     * If a new worker is needed, the worker base will be cloned.
     */
    public SimpleParallelDataRetriever(WorkerBuilder<DataRetrievalWorker<DataType>> workerBuilder, ThreadPoolExecutor executor) {
        super(executor);
        this.workerBuilder = workerBuilder;
    }
    
    @Override
    protected void setUpWorkersFor(RacingEventService racingService) {
        for (LeaderboardGroup group : racingService.getLeaderboardGroups().values()) {
            DataRetrievalWorker<DataType> worker = workerBuilder.build();
            worker.setReceiver(this);
            worker.setGroup(group);
            addWorker(worker);
        }
    }

    @Override
    protected Collection<DataType> finalizeData() {
        Collection<DataType> data = new ArrayList<DataType>();
        for (Collection<DataType> results : getResults()) {
            data.addAll(results);
        }
        return Collections.unmodifiableCollection(new CopyOnWriteArrayList<DataType>(data));
    }

}

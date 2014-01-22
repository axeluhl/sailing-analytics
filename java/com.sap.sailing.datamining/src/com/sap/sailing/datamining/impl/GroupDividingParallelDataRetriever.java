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

public class GroupDividingParallelDataRetriever<DataType> extends AbstractParallelComponent<Void, Collection<DataType>>
                                                          implements ParallelDataRetriever<DataType> {

    private RacingEventService racingService;
    private WorkerBuilder<DataRetrievalWorker<LeaderboardGroup, DataType>> workerBuilder;

    public GroupDividingParallelDataRetriever(RacingEventService racingService, WorkerBuilder<DataRetrievalWorker<LeaderboardGroup, DataType>> workerBuilder, ThreadPoolExecutor executor) {
        super(executor);
        this.racingService = racingService;
        this.workerBuilder = workerBuilder;
    }
    
    @Override
    protected void setUpWorkersFor(Void v) {
        for (LeaderboardGroup group : racingService.getLeaderboardGroups().values()) {
            DataRetrievalWorker<LeaderboardGroup, DataType> worker = workerBuilder.build();
            worker.setReceiver(this);
            worker.setSource(group);
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

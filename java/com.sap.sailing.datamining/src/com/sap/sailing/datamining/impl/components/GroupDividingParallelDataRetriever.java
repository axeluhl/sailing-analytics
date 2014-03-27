package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;

import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.components.ParallelDataRetriever;
import com.sap.sse.datamining.impl.components.deprecated.AbstractParallelComponent;
import com.sap.sse.datamining.workers.DataRetrievalWorker;
import com.sap.sse.datamining.workers.WorkerBuilder;

/**
 * Retrieves elements of type <code>DataType</code> from all {@link LeaderboardGroup}s known to the
 * {@link RacingEventSErvice} passed to this object's constructor. See also
 * {@link RacingEventService#getLeaderboardGroups()}.
 * 
 * @param <DataType>
 */
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

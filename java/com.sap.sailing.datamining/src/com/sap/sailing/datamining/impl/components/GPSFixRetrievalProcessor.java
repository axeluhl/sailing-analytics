package com.sap.sailing.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.Executor;

import com.sap.sailing.datamining.data.GPSFixWithContext;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractPartitioningParallelProcessor;

public class GPSFixRetrievalProcessor extends
        AbstractPartitioningParallelProcessor<RacingEventService, LeaderboardGroup, GPSFixWithContext> {

    public GPSFixRetrievalProcessor(Executor executor, Collection<Processor<GPSFixWithContext>> resultReceivers) {
        super(executor, resultReceivers);
    }

    @Override
    protected Runnable createInstruction(LeaderboardGroup partialElement) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Iterable<LeaderboardGroup> partitionElement(RacingEventService element) {
        return null;
    }

}

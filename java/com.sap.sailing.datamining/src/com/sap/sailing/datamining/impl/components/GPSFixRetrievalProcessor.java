package com.sap.sailing.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.GPSFixWithContext;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.AdditionalResultDataBuilder;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractPartitioningParallelProcessor;

public class GPSFixRetrievalProcessor extends
        AbstractPartitioningParallelProcessor<RacingEventService, LeaderboardGroup, GPSFixWithContext> {

    public GPSFixRetrievalProcessor(ExecutorService executor, Collection<Processor<GPSFixWithContext>> resultReceivers) {
        super(executor, resultReceivers);
    }

    @Override
    protected Callable<GPSFixWithContext> createInstruction(LeaderboardGroup partialElement) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Iterable<LeaderboardGroup> partitionElement(RacingEventService element) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void setAdditionalData(AdditionalResultDataBuilder additionalDataBuilder) {
        throw new UnsupportedOperationException();
    }

}

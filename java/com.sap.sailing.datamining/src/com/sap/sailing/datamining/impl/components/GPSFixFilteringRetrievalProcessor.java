package com.sap.sailing.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractFilteringRetrievalProcessor;

public class GPSFixFilteringRetrievalProcessor extends
        AbstractFilteringRetrievalProcessor<RacingEventService, LeaderboardGroup> {

    public GPSFixFilteringRetrievalProcessor(ExecutorService executor,
            Collection<Processor<LeaderboardGroup>> resultReceivers, FilterCriteria<LeaderboardGroup> criteria) {
        super(executor, resultReceivers, criteria);
    }

    @Override
    protected Iterable<LeaderboardGroup> retrieveData(RacingEventService element) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Callable<LeaderboardGroup> createInstruction(LeaderboardGroup filteredPartialElement) {
        // TODO Auto-generated method stub
        return null;
    }

}

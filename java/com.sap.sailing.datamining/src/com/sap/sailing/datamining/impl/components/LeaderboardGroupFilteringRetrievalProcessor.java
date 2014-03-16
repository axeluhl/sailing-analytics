package com.sap.sailing.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractFilteringRetrievalProcessor;

public class LeaderboardGroupFilteringRetrievalProcessor extends
        AbstractFilteringRetrievalProcessor<RacingEventService, LeaderboardGroup> {

    public LeaderboardGroupFilteringRetrievalProcessor(ExecutorService executor,
            Collection<Processor<LeaderboardGroup>> resultReceivers, FilterCriteria<LeaderboardGroup> criteria) {
        super(executor, resultReceivers, criteria);
    }

    @Override
    protected Iterable<LeaderboardGroup> retrieveData(RacingEventService element) {
        return element.getLeaderboardGroups().values();
    }

}

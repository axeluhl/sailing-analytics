package com.sap.sailing.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasLeaderboardGroupContext;
import com.sap.sailing.datamining.data.LeaderboardGroupWithContext;
import com.sap.sailing.datamining.impl.data.HasLeaderboardGroupContextImpl;
import com.sap.sailing.datamining.impl.data.LeaderboardGroupWithContextImpl;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractFilteringRetrievalProcessor;

public class LeaderboardGroupFilteringRetrievalProcessor extends
        AbstractFilteringRetrievalProcessor<RacingEventService, LeaderboardGroup, LeaderboardGroupWithContext> {

    public LeaderboardGroupFilteringRetrievalProcessor(ExecutorService executor,
            Collection<Processor<LeaderboardGroupWithContext>> resultReceivers, FilterCriteria<LeaderboardGroupWithContext> criteria) {
        super(executor, resultReceivers, criteria);
    }

    @Override
    protected Iterable<LeaderboardGroup> retrieveData(RacingEventService element) {
        return element.getLeaderboardGroups().values();
    }

    @Override
    protected LeaderboardGroupWithContext contextifyElement(LeaderboardGroup partialElement) {
        HasLeaderboardGroupContext context = new HasLeaderboardGroupContextImpl(partialElement);
        return new LeaderboardGroupWithContextImpl(context);
    }

}

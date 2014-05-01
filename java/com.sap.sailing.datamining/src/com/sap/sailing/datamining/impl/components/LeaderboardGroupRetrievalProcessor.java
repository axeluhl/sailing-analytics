package com.sap.sailing.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class LeaderboardGroupRetrievalProcessor extends
        AbstractRetrievalProcessor<RacingEventService, LeaderboardGroup, LeaderboardGroup> {

    public LeaderboardGroupRetrievalProcessor(ExecutorService executor,
            Collection<Processor<LeaderboardGroup>> resultReceivers) {
        super(executor, resultReceivers);
    }

    @Override
    protected LeaderboardGroup convertWorkingToResultType(LeaderboardGroup partialElement) {
        return partialElement;
    }

    @Override
    protected Iterable<LeaderboardGroup> retrieveData(RacingEventService element) {
        return element.getLeaderboardGroups().values();
    }

}

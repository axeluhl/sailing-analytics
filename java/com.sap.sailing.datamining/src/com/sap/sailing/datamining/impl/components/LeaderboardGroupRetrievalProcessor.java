package com.sap.sailing.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractSimpleRetrievalProcessor;

public class LeaderboardGroupRetrievalProcessor extends
        AbstractSimpleRetrievalProcessor<RacingEventService, LeaderboardGroup> {

    public LeaderboardGroupRetrievalProcessor(ExecutorService executor,
            Collection<Processor<LeaderboardGroup, ?>> resultReceivers) {
        super(RacingEventService.class, LeaderboardGroup.class, executor, resultReceivers);
    }

    @Override
    protected Iterable<LeaderboardGroup> retrieveData(RacingEventService element) {
        return element.getLeaderboardGroups().values();
    }

}

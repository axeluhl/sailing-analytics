package com.sap.sailing.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractSimpleRetrievalProcessor;

public class LeaderboardRetrievalProcessor extends
        AbstractSimpleRetrievalProcessor<LeaderboardGroup, Leaderboard> {

    public LeaderboardRetrievalProcessor(ExecutorService executor,
            Collection<Processor<Leaderboard, ?>> resultReceivers) {
        super(LeaderboardGroup.class, Leaderboard.class, executor, resultReceivers);
    }

    @Override
    protected Iterable<Leaderboard> retrieveData(LeaderboardGroup element) {
        return element.getLeaderboards();
    }

}

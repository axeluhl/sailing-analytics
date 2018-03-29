package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasLeaderboardContext;
import com.sap.sailing.datamining.impl.data.LeaderboardGroupWithContext;
import com.sap.sailing.datamining.impl.data.LeaderboardWithContext;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class LeaderboardRetrievalProcessor extends AbstractRetrievalProcessor<LeaderboardGroupWithContext, HasLeaderboardContext> {

    public LeaderboardRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasLeaderboardContext, ?>> resultReceivers, int retrievalLevel) {
        super(LeaderboardGroupWithContext.class, HasLeaderboardContext.class, executor, resultReceivers, retrievalLevel);
    }

    @Override
    protected Iterable<HasLeaderboardContext> retrieveData(LeaderboardGroupWithContext element) {
        Collection<HasLeaderboardContext> leaderboardsWithContext = new ArrayList<>();
        for (Leaderboard leaderboard : element.getLeaderboardGroup().getLeaderboards()) {
            leaderboardsWithContext.add(new LeaderboardWithContext(leaderboard, element, element.getPolarDataService()));
        }
        return leaderboardsWithContext;
    }

}

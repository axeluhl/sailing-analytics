package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasLeaderboardContext;
import com.sap.sailing.datamining.impl.data.LeaderboardWithContext;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractSimpleRetrievalProcessor;

public class LeaderboardRetrievalProcessor extends
        AbstractSimpleRetrievalProcessor<LeaderboardGroup, HasLeaderboardContext> {

    public LeaderboardRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasLeaderboardContext, ?>> resultReceivers) {
        super(LeaderboardGroup.class, HasLeaderboardContext.class, executor, resultReceivers);
    }

    @Override
    protected Iterable<HasLeaderboardContext> retrieveData(LeaderboardGroup element) {
        Collection<HasLeaderboardContext> leaderboardsWithContext = new ArrayList<>();
        for (Leaderboard leaderboard : element.getLeaderboards()) {
            leaderboardsWithContext.add(new LeaderboardWithContext(leaderboard));
        }
        return leaderboardsWithContext;
    }

}

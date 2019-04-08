package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasLeaderboardContext;
import com.sap.sailing.datamining.data.HasLeaderboardGroupContext;
import com.sap.sailing.datamining.impl.data.LeaderboardWithContext;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class LeaderboardRetrievalProcessor extends AbstractRetrievalProcessor<HasLeaderboardGroupContext, HasLeaderboardContext> {

    public LeaderboardRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasLeaderboardContext, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(HasLeaderboardGroupContext.class, HasLeaderboardContext.class, executor, resultReceivers, retrievalLevel,
                retrievedDataTypeMessageKey);
    }

    @Override
    protected Iterable<HasLeaderboardContext> retrieveData(HasLeaderboardGroupContext element) {
        Collection<HasLeaderboardContext> leaderboardsWithContext = new ArrayList<>();
        for (Leaderboard leaderboard : element.getLeaderboardGroup().getLeaderboards()) {
            if (isAborted()) {
                break;
            }
            leaderboardsWithContext.add(new LeaderboardWithContext(leaderboard, element));
        }
        return leaderboardsWithContext;
    }

}

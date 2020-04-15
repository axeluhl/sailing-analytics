package com.sap.sailing.polars.datamining.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.polars.datamining.data.HasLeaderboardGroupPolarContext;
import com.sap.sailing.polars.datamining.data.HasLeaderboardPolarContext;
import com.sap.sailing.polars.datamining.data.impl.LeaderboardWithPolarContext;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class PolarLeaderboardRetrievalProcessor extends AbstractRetrievalProcessor<HasLeaderboardGroupPolarContext, HasLeaderboardPolarContext> {

    public PolarLeaderboardRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasLeaderboardPolarContext, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(HasLeaderboardGroupPolarContext.class, HasLeaderboardPolarContext.class, executor, resultReceivers,
                retrievalLevel, retrievedDataTypeMessageKey);
    }

    @Override
    protected Iterable<HasLeaderboardPolarContext> retrieveData(HasLeaderboardGroupPolarContext element) {
        Set<HasLeaderboardPolarContext> leaderboardsWithContext = new HashSet<>();
        for (Leaderboard leaderboard : element.getLeaderboardGroup().getLeaderboards()) {
            if (isAborted()) {
                break;
            }
            leaderboardsWithContext.add(new LeaderboardWithPolarContext(leaderboard, element));
        }
        return leaderboardsWithContext;
    }

}

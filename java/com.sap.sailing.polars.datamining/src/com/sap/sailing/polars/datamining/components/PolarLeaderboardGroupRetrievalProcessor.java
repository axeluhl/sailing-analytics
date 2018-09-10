package com.sap.sailing.polars.datamining.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.polars.datamining.data.HasLeaderboardGroupPolarContext;
import com.sap.sailing.polars.datamining.data.impl.LeaderboardGroupWithPolarContext;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class PolarLeaderboardGroupRetrievalProcessor extends AbstractRetrievalProcessor<RacingEventService, HasLeaderboardGroupPolarContext> {

    public PolarLeaderboardGroupRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasLeaderboardGroupPolarContext, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(RacingEventService.class, HasLeaderboardGroupPolarContext.class, executor, resultReceivers,
                retrievalLevel, retrievedDataTypeMessageKey);
    }

    @Override
    protected Iterable<HasLeaderboardGroupPolarContext> retrieveData(RacingEventService element) {
        Set<HasLeaderboardGroupPolarContext> data = new HashSet<>();
        for (LeaderboardGroup leaderboardGroup : element.getLeaderboardGroups().values()) {
            if (isAborted()) {
                break;
            }
            data.add(new LeaderboardGroupWithPolarContext(leaderboardGroup));
        }
        return data;
    }

}

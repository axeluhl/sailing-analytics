package com.sap.sailing.polars.datamining.components;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.data.HasLeaderboardGroupContext;
import com.sap.sailing.datamining.impl.data.LeaderboardGroupWithContext;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.interfaces.RacingEventService;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class PolarLeaderboardGroupRetrievalProcessor extends AbstractRetrievalProcessor<RacingEventService, HasLeaderboardGroupContext> {
    public PolarLeaderboardGroupRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasLeaderboardGroupContext, ?>> resultReceivers, int retrievalLevel,
            String retrievedDataTypeMessageKey) {
        super(RacingEventService.class, HasLeaderboardGroupContext.class, executor, resultReceivers,
                retrievalLevel, retrievedDataTypeMessageKey);
    }

    @Override
    protected Iterable<HasLeaderboardGroupContext> retrieveData(RacingEventService element) {
        Set<HasLeaderboardGroupContext> data = new HashSet<>();
        for (LeaderboardGroup leaderboardGroup : element.getLeaderboardGroups().values()) {
            if (isAborted()) {
                break;
            }
            data.add(new LeaderboardGroupWithContext(leaderboardGroup, element.getPolarDataService(), element.getBaseDomainFactory(), element.getSecurityService()));
        }
        return data;
    }

}

package com.sap.sailing.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import com.sap.sailing.datamining.impl.data.LeaderboardGroupWithContext;
import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class LeaderboardGroupRetrievalProcessor extends AbstractRetrievalProcessor<RacingEventService, LeaderboardGroupWithContext> {

    public LeaderboardGroupRetrievalProcessor(ExecutorService executor,
            Collection<Processor<LeaderboardGroupWithContext, ?>> resultReceivers, int retrievalLevel) {
        super(RacingEventService.class, LeaderboardGroupWithContext.class, executor, resultReceivers, retrievalLevel);
    }

    @Override
    protected Iterable<LeaderboardGroupWithContext> retrieveData(RacingEventService element) {
        final PolarDataService polarDataService = element.getPolarDataService();
        return element.getLeaderboardGroups()
                .values()
                .stream()
                .map(lg -> new LeaderboardGroupWithContext(lg, polarDataService))
                .collect(Collectors.toSet());
    }

}

package com.sap.sailing.polars.datamining.components;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import com.sap.sailing.polars.datamining.data.HasLeaderboardGroupPolarContext;
import com.sap.sailing.polars.datamining.data.impl.LeaderboardGroupWithPolarContext;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractRetrievalProcessor;

public class PolarLeaderboardGroupRetrievalProcessor extends AbstractRetrievalProcessor<RacingEventService, HasLeaderboardGroupPolarContext> {

    public PolarLeaderboardGroupRetrievalProcessor(ExecutorService executor,
            Collection<Processor<HasLeaderboardGroupPolarContext, ?>> resultReceivers, int retrievalLevel) {
        super(RacingEventService.class, HasLeaderboardGroupPolarContext.class, executor, resultReceivers, retrievalLevel);
    }

    @Override
    protected Iterable<HasLeaderboardGroupPolarContext> retrieveData(RacingEventService element) {
        return element.getLeaderboardGroups()
                .values()
                .stream()
                .map(lg -> new LeaderboardGroupWithPolarContext(lg))
                .collect(Collectors.toSet());
    }

}

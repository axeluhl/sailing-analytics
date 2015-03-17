package com.sap.sailing.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import com.sap.sailing.datamining.impl.data.LeaderboardGroupWithContext;
import com.sap.sailing.polars.PolarDataService;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractSimpleRetrievalProcessor;

public class LeaderboardGroupRetrievalProcessor extends
        AbstractSimpleRetrievalProcessor<RacingEventService, LeaderboardGroupWithContext> {

    public LeaderboardGroupRetrievalProcessor(ExecutorService executor,
            Collection<Processor<LeaderboardGroupWithContext, ?>> resultReceivers) {
        super(RacingEventService.class, LeaderboardGroupWithContext.class, executor, resultReceivers);
    }

    @Override
    protected Iterable<LeaderboardGroupWithContext> retrieveData(RacingEventService element) {
		final PolarDataService polarDataService = element.getPolarDataService();
		return element
				.getLeaderboardGroups()
				.values()
				.stream()
				.map(lg -> new LeaderboardGroupWithContext(lg, polarDataService))
				.collect(Collectors.toSet());
	}

}

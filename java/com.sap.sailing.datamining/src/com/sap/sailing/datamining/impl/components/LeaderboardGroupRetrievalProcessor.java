package com.sap.sailing.datamining.impl.components;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.datamining.Activator;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.server.RacingEventService;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractSimpleRetrievalProcessor;
import com.sap.sse.datamining.shared.annotations.DataRetriever;

@DataRetriever(dataType=LeaderboardGroup.class,
               groupName=Activator.dataRetrieverGroupName,
               level=0)
public class LeaderboardGroupRetrievalProcessor extends
        AbstractSimpleRetrievalProcessor<RacingEventService, LeaderboardGroup> {

    public LeaderboardGroupRetrievalProcessor(ExecutorService executor,
            Collection<Processor<LeaderboardGroup, ?>> resultReceivers) {
        super(RacingEventService.class, LeaderboardGroup.class, executor, resultReceivers);
    }

    @Override
    protected Iterable<LeaderboardGroup> retrieveData(RacingEventService element) {
        return element.getLeaderboardGroups().values();
    }

}

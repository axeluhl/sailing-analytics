package com.sap.sailing.datamining.impl.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.leaderboard.RegattaLeaderboard;
import com.sap.sse.datamining.components.FilterCriteria;
import com.sap.sse.datamining.components.Processor;
import com.sap.sse.datamining.impl.components.AbstractSimpleFilteringRetrievalProcessor;

public class RegattaLeaderboardRetrievalProcessor extends
        AbstractSimpleFilteringRetrievalProcessor<LeaderboardGroup, RegattaLeaderboard> {

    public RegattaLeaderboardRetrievalProcessor(ExecutorService executor,
            Collection<Processor<RegattaLeaderboard>> resultReceivers, FilterCriteria<RegattaLeaderboard> criteria) {
        super(executor, resultReceivers, criteria);
    }

    @Override
    protected Iterable<RegattaLeaderboard> retrieveData(LeaderboardGroup element) {
        Collection<RegattaLeaderboard> regattaLeaderboards = new ArrayList<>();
        for (Leaderboard leaderboard : element.getLeaderboards()) {
            if (leaderboard instanceof RegattaLeaderboard) {
                regattaLeaderboards.add((RegattaLeaderboard) leaderboard);
            }
        }
        return regattaLeaderboards;
    }

}

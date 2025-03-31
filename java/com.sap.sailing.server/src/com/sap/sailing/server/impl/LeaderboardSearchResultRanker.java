package com.sap.sailing.server.impl;

import com.sap.sailing.domain.base.LeaderboardSearchResult;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.tracking.TrackedRegatta;
import com.sap.sailing.domain.tracking.TrackedRegattaRegistry;
import com.sap.sailing.server.interfaces.RacingEventService;

public class LeaderboardSearchResultRanker extends LeaderboardSearchResultBaseRanker<LeaderboardSearchResult> {
    private final TrackedRegattaRegistry trackedRegattaRegistry;
    
    protected LeaderboardSearchResultRanker(RacingEventService racingEventService) {
        this.trackedRegattaRegistry = racingEventService;
    }

    protected TrackedRegatta getTrackedRegatta(LeaderboardSearchResult o1) {
        Regatta r1 = o1.getRegatta();
        TrackedRegatta trackedR1 = r1 == null ? null : trackedRegattaRegistry.getTrackedRegatta(r1);
        return trackedR1;
    }
}

package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasLeaderboardGroupContext;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.polars.PolarDataService;

public class LeaderboardGroupWithContext implements HasLeaderboardGroupContext {
    private final LeaderboardGroup leaderboardGroup;
    private final PolarDataService polarDataService;
    private final DomainFactory baseDomainFactory;

    public LeaderboardGroupWithContext(LeaderboardGroup leaderboardGroup, PolarDataService polarDataService, DomainFactory baseDomainFactory) {
        this.leaderboardGroup = leaderboardGroup;
        this.polarDataService = polarDataService;
        this.baseDomainFactory = baseDomainFactory;
    }

    @Override
    public LeaderboardGroup getLeaderboardGroup() {
        return leaderboardGroup;
    }

    @Override
    public PolarDataService getPolarDataService() {
        return polarDataService;
    }

    @Override
    public DomainFactory getBaseDomainFactory() {
        return baseDomainFactory;
    }
}

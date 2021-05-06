package com.sap.sailing.polars.datamining.data.impl;

import com.sap.sailing.datamining.data.HasLeaderboardGroupContext;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.polars.PolarDataService;

public class LeaderboardGroupWithPolarContext implements HasLeaderboardGroupContext {
    
    private final LeaderboardGroup leaderboardGroup;
    private final DomainFactory baseDomainFactory;
    private final PolarDataService polarDataService;
    
    public LeaderboardGroupWithPolarContext(LeaderboardGroup leaderboardGroup, PolarDataService polarDataService, DomainFactory baseDomainFactory) {
        this.leaderboardGroup = leaderboardGroup;
        this.polarDataService = polarDataService;
        this.baseDomainFactory = baseDomainFactory;
    }

    @Override
    public LeaderboardGroup getLeaderboardGroup() {
        return leaderboardGroup;
    }

    @Override
    public DomainFactory getBaseDomainFactory() {
        return baseDomainFactory;
    }

    @Override
    public PolarDataService getPolarDataService() {
        return polarDataService;
    }
}

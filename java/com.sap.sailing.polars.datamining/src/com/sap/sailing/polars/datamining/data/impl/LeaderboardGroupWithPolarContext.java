package com.sap.sailing.polars.datamining.data.impl;

import com.sap.sailing.datamining.data.HasLeaderboardGroupContext;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.leaderboard.LeaderboardGroup;
import com.sap.sailing.domain.polars.PolarDataService;
import com.sap.sse.security.SecurityService;

public class LeaderboardGroupWithPolarContext implements HasLeaderboardGroupContext {
    private final LeaderboardGroup leaderboardGroup;
    private final DomainFactory baseDomainFactory;
    private final PolarDataService polarDataService;
    private final SecurityService securityService;
    
    public LeaderboardGroupWithPolarContext(LeaderboardGroup leaderboardGroup, PolarDataService polarDataService, DomainFactory baseDomainFactory, SecurityService securityService) {
        this.leaderboardGroup = leaderboardGroup;
        this.polarDataService = polarDataService;
        this.baseDomainFactory = baseDomainFactory;
        this.securityService = securityService;
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

    @Override
    public SecurityService getSecurityService() {
        return securityService;
    }
}

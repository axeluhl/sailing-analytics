package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasLeaderboardContext;
import com.sap.sailing.datamining.data.HasLeaderboardGroupContext;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.polars.PolarDataService;

public class LeaderboardWithContext implements HasLeaderboardContext {
    private final Leaderboard leaderboard;
    private final HasLeaderboardGroupContext leaderboardGroupContext;
    private final PolarDataService polarDataService;

    public LeaderboardWithContext(Leaderboard leaderboard, HasLeaderboardGroupContext leaderboardGroupContext, PolarDataService polarDataService) {
        this.leaderboard = leaderboard;
        this.leaderboardGroupContext = leaderboardGroupContext;
        this.polarDataService = polarDataService;
    }

    public HasLeaderboardGroupContext getLeaderboardGroupContext() {
        return leaderboardGroupContext;
    }
    
    @Override
    public Leaderboard getLeaderboard() {
        return leaderboard;
    }
    
    @Override
    public BoatClass getBoatClass() {
        return getLeaderboard().getBoatClass();
    }

    @Override
    public PolarDataService getPolarDataService() {
        return polarDataService;
    }

    @Override
    public String getName() {
        return getLeaderboard().getName();
    }
}

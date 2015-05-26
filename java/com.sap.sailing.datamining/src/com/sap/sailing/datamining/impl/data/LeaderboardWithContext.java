package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasLeaderboardContext;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sailing.domain.polars.PolarDataService;

public class LeaderboardWithContext implements HasLeaderboardContext {

    private final Leaderboard leaderboard;
    private final PolarDataService polarDataService;

    public LeaderboardWithContext(Leaderboard leaderboard, PolarDataService polarDataService) {
        this.leaderboard = leaderboard;
        this.polarDataService = polarDataService;
    }

    @Override
    public Leaderboard getLeaderboard() {
        return leaderboard;
    }

	@Override
	public PolarDataService getPolarDataService() {
		return polarDataService;
	}

}

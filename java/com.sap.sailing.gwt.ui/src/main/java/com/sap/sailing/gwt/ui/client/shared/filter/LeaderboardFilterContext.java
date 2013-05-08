package com.sap.sailing.gwt.ui.client.shared.filter;

import com.sap.sailing.gwt.ui.leaderboard.LeaderboardFetcher;

public interface LeaderboardFilterContext {
    void setLeaderboardFetcher(LeaderboardFetcher leaderboardFetcher);

    LeaderboardFetcher getLeaderboardFetcher();
}

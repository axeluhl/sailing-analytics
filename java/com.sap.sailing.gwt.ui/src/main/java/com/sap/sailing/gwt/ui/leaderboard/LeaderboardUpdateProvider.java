package com.sap.sailing.gwt.ui.leaderboard;

import com.sap.sailing.gwt.ui.client.LeaderboardUpdateListener;

public interface LeaderboardUpdateProvider {
    void addLeaderboardUpdateListener(LeaderboardUpdateListener listener);

    void removeLeaderboardUpdateListener(LeaderboardUpdateListener listener);
}

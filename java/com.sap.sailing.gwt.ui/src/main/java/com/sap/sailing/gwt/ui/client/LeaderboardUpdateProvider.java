package com.sap.sailing.gwt.ui.client;

public interface LeaderboardUpdateProvider {
    void addLeaderboardUpdateListener(LeaderboardUpdateListener listener);

    void removeLeaderboardUpdateListener(LeaderboardUpdateListener listener);
}

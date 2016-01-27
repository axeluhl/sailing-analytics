package com.sap.sailing.gwt.ui.client;


public interface LeaderboardUpdateProvider {
    public void addLeaderboardUpdateListener(LeaderboardUpdateListener listener);

    public void removeLeaderboardUpdateListener(LeaderboardUpdateListener listener);
}

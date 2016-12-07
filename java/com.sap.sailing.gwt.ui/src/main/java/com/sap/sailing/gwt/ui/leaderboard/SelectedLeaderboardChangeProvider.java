package com.sap.sailing.gwt.ui.leaderboard;


public interface SelectedLeaderboardChangeProvider {

    void addSelectedLeaderboardChangeListener(SelectedLeaderboardChangeListener listener);

    void removeSelectedLeaderboardChangeListener(SelectedLeaderboardChangeListener listener);
    
    void setSelectedLeaderboard(LeaderboardPanel selectedLeaderboard);
}

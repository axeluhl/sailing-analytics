package com.sap.sailing.gwt.ui.leaderboard;


public interface SelectedLeaderboardChangeProvider <T extends LeaderboardPanel<?>> {

    void addSelectedLeaderboardChangeListener(SelectedLeaderboardChangeListener<T> listener);

    void removeSelectedLeaderboardChangeListener(SelectedLeaderboardChangeListener<T> listener);
    
    void setSelectedLeaderboard(MultiRaceLeaderboardPanel selectedLeaderboard);
}

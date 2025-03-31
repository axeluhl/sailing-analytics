package com.sap.sailing.gwt.ui.leaderboard;


/**
 * Allows UI components to observe a change of the selected leaderboard in a Multi-Leaderboard scenario.
 */
public interface SelectedLeaderboardChangeListener<T extends LeaderboardPanel<?>> {

    /**
     * @param selectedLeaderboard the selected non-<code>null</code> LeaderboardPanel 
     */
    void onSelectedLeaderboardChanged(T selectedLeaderboard);

}

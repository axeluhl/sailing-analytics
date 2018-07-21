package com.sap.sailing.gwt.ui.client;

import java.util.Map;

import com.sap.sailing.gwt.ui.shared.LeaderboardGroupDTO;

public interface LeaderboardGroupsRefresher {
    /**
     * Fetch all leaderboard groups from the server and distribute to all {@link LeaderboardGroupsDisplayer}s registered.
     */
    void fillLeaderboardGroups();
    
    /**
     * Update all {@link LeaderboardGroupsDisplayer}s registered with the updated collection of leaderboard groups.
     * Calling this method is useful for notifying all other {@link LeaderboardGroupsDisplayer}s after one area of the
     * UI has actively changed the leaderboard groups collection and has already updated its sequence of
     * {@link LeaderboardGroupDTO}s. This does not necessarily create consistency with the leaderboard groups known by
     * the server at that time, but at least all areas of the UI that have registered as
     * {@link LeaderboardGroupsDisplayer} will show an equal sequence of leaderboard groups.
     * 
     * @param origin
     *            will not receive a call to its {@link LeaderboardGroupsDisplayer#fillLeaderboardGroups(Iterable)}, assuming that
     *            the update originated in <code>origin</code> and hence no notification is required.
     */
    void updateLeaderboardGroups(Iterable<LeaderboardGroupDTO> updatedLeaderboardGroups, LeaderboardGroupsDisplayer origin);
    
    /**
     * Setup the state {@link LeaderboardGroupsDisplayer} using parameters
     * 
     *  @param params
     *          {@link Map} {@link String} parameters to setup the displayer
     */
    public void setupLeaderboardGroups(LeaderboardGroupsDisplayer displayer, Map<String, String> params);

}

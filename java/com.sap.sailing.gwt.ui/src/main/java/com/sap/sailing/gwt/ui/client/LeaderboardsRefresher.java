package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public interface LeaderboardsRefresher<T extends StrippedLeaderboardDTO> {
    /**
     * Fetch all leaderboards from the server as {@link StrippedLeaderboardDTO} objects and distribute to all
     * {@link LeaderboardsDisplayer}s registered.
     */
    void fillLeaderboards();
    
    /**
     * Update all {@link LeaderboardsDisplayer}s registered with the updated collection of leaderboards. Calling this
     * method is useful for notifying all other {@link LeaderboardsDisplayer}s after one area of the UI has actively
     * changed the leaderboard collection and has already updated its sequence of {@link StrippedLeaderboardDTO}s.
     * This does not necessarily create consistency with the leaderboards known by the server at that time, but at
     * least all areas of the UI that have registered as {@link LeaderboardsDisplayer} will show an equal sequence
     * of leaderboards.
     * 
     * @param origin will not receive a call to its {@link LeaderboardsDisplayer#fillLeaderboards(Iterable)}, assuming
     * that the update originated in <code>origin</code> and hence no notification is required.
     */
    void updateLeaderboards(Iterable<T> updatedLeaderboards, LeaderboardsDisplayer<T> origin);
}

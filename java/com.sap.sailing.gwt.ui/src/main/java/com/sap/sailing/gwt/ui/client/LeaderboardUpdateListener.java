package com.sap.sailing.gwt.ui.client;

import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.raceboard.RaceBoardPanel;

/**
 * Listener interface that gets information about refresh of leaderboard.
 * Currently implemented by {@link RaceBoardPanel} and triggered by
 * {@link ClassicLeaderboardPanel}.
 * 
 * @author Simon Marcel Pamies
 */
public interface LeaderboardUpdateListener {
    
    /**
     * Called whenever the leaderboard has been updated.
     */
    void updatedLeaderboard(LeaderboardDTO leaderboard);
    
    /**
     * Called whenever a current race for the leaderboard could
     * be determined. One can not rely on this method being called
     * as a leaderboard could contain more than one race.
     */
    void currentRaceSelected(RaceIdentifier raceIdentifier, RaceColumnDTO raceColumn);
}

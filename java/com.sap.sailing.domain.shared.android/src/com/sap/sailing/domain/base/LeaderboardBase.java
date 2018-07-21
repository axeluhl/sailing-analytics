package com.sap.sailing.domain.base;

import com.sap.sse.common.Named;

public interface LeaderboardBase extends Named {
    /**
     * If a display name for the leaderboard has been defined,
     * this method returns it; otherwise, <code>null</code> is returned.
     */
    String getDisplayName();
    
    void addLeaderboardChangeListener(LeaderboardChangeListener listener);
    
    void removeLeaderboardChangeListener(LeaderboardChangeListener listener);
}

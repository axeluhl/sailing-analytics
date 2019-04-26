package com.sap.sailing.domain.base;

import com.sap.sse.common.Named;
import com.sap.sse.security.shared.WithQualifiedObjectIdentifier;

public interface LeaderboardBase extends Named, WithQualifiedObjectIdentifier {
    /**
     * If a display name for the leaderboard has been defined,
     * this method returns it; otherwise, <code>null</code> is returned.
     */
    String getDisplayName();
    
    void addLeaderboardChangeListener(LeaderboardChangeListener listener);
    
    void removeLeaderboardChangeListener(LeaderboardChangeListener listener);
}

package com.sap.sailing.datamining.data;

import com.sap.sse.datamining.shared.annotations.Dimension;

public interface LeaderboardGroupWithContext {
    
    @Dimension(messageKey="leaderboardGroup")
    public String getLeaderboardGroupName();

}

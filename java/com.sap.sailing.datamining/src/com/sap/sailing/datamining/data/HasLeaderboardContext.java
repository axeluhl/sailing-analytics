package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;

public interface HasLeaderboardContext {
    @Connector(scanForStatistics=false)
    public HasLeaderboardGroupContext getLeaderboardGroupContext();
    
    Leaderboard getLeaderboard();

    @Dimension(messageKey="Leaderboard", ordinal=1)
    default String getName() {
        return getLeaderboard().getName();
    }
    
    @Connector(messageKey="BoatClass", ordinal=2)
    default BoatClass getBoatClass() {
        return getLeaderboard().getBoatClass();
    }
}

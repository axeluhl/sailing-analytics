package com.sap.sailing.polars.datamining.data;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sse.datamining.annotations.Connector;

public interface HasLeaderboardPolarContext {
    
    @Connector(scanForStatistics=false)
    HasLeaderboardGroupPolarContext getLeaderboardGroupPolarContext();
    
    @Connector(messageKey="Leaderboard", ordinal=1)
    Leaderboard getLeaderboard();
    
    @Connector(messageKey="BoatClass", ordinal=2)
    BoatClass getBoatClass();

}

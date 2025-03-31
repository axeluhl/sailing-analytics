package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.leaderboard.Leaderboard;
import com.sap.sse.common.Util;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;

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
    
    @Statistic(messageKey="NumberOfCompetitors", ordinal=3)
    default int getNumberOfCompetitors() {
        return Util.size(getLeaderboard().getCompetitors());
    }
}

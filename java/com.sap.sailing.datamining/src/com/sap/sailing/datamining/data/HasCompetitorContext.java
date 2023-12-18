package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.datamining.annotations.Connector;

public interface HasCompetitorContext {
    @Connector(messageKey="Leaderboard")
    HasLeaderboardContext getLeaderboardContext();

    @Connector
    Competitor getCompetitor();
}

package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.datamining.shared.annotations.Connector;
import com.sap.sse.datamining.shared.annotations.Statistic;

public interface HasRaceResultOfCompetitorContext {
    
    @Connector(scanForStatistics=false)
    public HasLeaderboardContext getLeaderboardContext();

    @Connector(messageKey="Competitor")
    public Competitor getCompetitor();
    
    /**
     * 0 means the competitor won the race, 1 means the competitor ranked last
     */
    @Statistic(messageKey="RelativeRankInRace", ordinal=3)
    public double getRelativeRank();

}

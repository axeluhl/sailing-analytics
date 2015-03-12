package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.datamining.shared.annotations.Connector;
import com.sap.sse.datamining.shared.annotations.Dimension;
import com.sap.sse.datamining.shared.annotations.Statistic;

public interface HasRaceResultOfCompetitorContext {
    
    @Connector(scanForStatistics=false)
    public HasLeaderboardContext getLeaderboardContext();

    @Connector(messageKey="Competitor")
    public Competitor getCompetitor();
    
    /**
     * 0 means the competitor won the race, 1 means the competitor ranked last
     */
    @Statistic(messageKey="RelativeScoreInRace", ordinal=3)
    public double getRelativeRank();

    @Dimension(messageKey="WindSpeedInBeaufort")
    int getAverageWindSpeedInRoundedBeaufort();

    @Dimension(messageKey="Regatta")
    String getRegattaName();
}

package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;

public interface HasRaceResultOfCompetitorContext {
    
    @Connector(scanForStatistics=false)
    public HasLeaderboardContext getLeaderboardContext();

    @Connector(messageKey="Competitor")
    public Competitor getCompetitor();
    
    /**
     * 0 means the competitor won the race, 1 means the competitor ranked last
     */
    @Statistic(messageKey="RelativeScoreInRace", ordinal=1, resultDecimals=2)
    public double getRelativeRank();

    @Dimension(messageKey="WindSpeedInBeaufort")
    int getAverageWindSpeedInRoundedBeaufort();

    @Dimension(messageKey="Regatta")
    String getRegattaName();
    
    @Statistic(messageKey="NumberOfPodiumFinish", ordinal=2)
    public Boolean isPodiumFinish();
    
    @Statistic(messageKey="NumberOfWins", ordinal=3)
    public Boolean isWin();
}

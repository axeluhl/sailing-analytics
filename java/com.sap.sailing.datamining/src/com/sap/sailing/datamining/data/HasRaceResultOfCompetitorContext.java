package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;

public interface HasRaceResultOfCompetitorContext {
    
    @Connector(scanForStatistics=false)
    public HasLeaderboardContext getLeaderboardContext();

    @Connector(messageKey="Competitor")
    public Competitor getCompetitor();
    
    @Dimension(messageKey="CompetitorSearchTag", ordinal=11) // TODO Clean me: Move Dimension to Competitor when possible
    public String getCompetitorSearchTag();
    
    /**
     * 0 means the competitor won the race, 1 means the competitor ranked last
     */
    @Statistic(messageKey="RelativeScore", ordinal=1, resultDecimals=2)
    public double getRelativeRank();
    
    @Statistic(messageKey="AbsoluteRank", ordinal=2, resultDecimals=2)
    public double getAbsoluteRank() throws NoWindException;

    @Dimension(messageKey="WindSpeedInBeaufort")
    int getAverageWindSpeedInRoundedBeaufort();

    @Dimension(messageKey="Regatta")
    String getRegattaName();
    
    @Statistic(messageKey="NumberOfPodiumFinish", ordinal=3)
    public Boolean isPodiumFinish();
    
    @Statistic(messageKey="NumberOfWins", ordinal=4)
    public Boolean isWin();
}

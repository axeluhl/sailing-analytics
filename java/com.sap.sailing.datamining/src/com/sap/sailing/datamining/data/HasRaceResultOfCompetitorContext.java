package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;

public interface HasRaceResultOfCompetitorContext {
    
    @Connector(scanForStatistics=false)
    public HasLeaderboardContext getLeaderboardContext();

    @Connector(messageKey="Competitor")
    public Competitor getCompetitor();
    
    @Connector(messageKey="BoatClass")
    BoatClass getBoatClass();

    @Dimension(messageKey="Regatta")
    String getRegattaName();
    
    @Dimension(messageKey="CompetitorSearchTag", ordinal=11) // TODO Clean me: Move Dimension to Competitor when possible
    public String getCompetitorSearchTag();

    @Dimension(messageKey="WindSpeedInBeaufort")
    int getAverageWindSpeedInRoundedBeaufort();
    
    /**
     * 0 means the competitor won the race, 1 means the competitor ranked last
     */
    @Statistic(messageKey="RelativeScore", ordinal=1, resultDecimals=2)
    public Double getRelativeRank();
    
    @Statistic(messageKey="AbsoluteRank", ordinal=2, resultDecimals=2)
    public Double getAbsoluteRank();
    
    @Statistic(messageKey="NumberOfPodiumFinish", ordinal=3)
    public Boolean isPodiumFinish();
    
    @Statistic(messageKey="NumberOfWins", ordinal=4)
    public Boolean isWin();
}

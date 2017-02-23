package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;
import com.sap.sse.datamining.shared.impl.dto.ClusterDTO;

public interface HasRaceResultOfCompetitorContext {
    
    @Connector(scanForStatistics=false)
    public HasLeaderboardContext getLeaderboardContext();

    @Connector(messageKey="Competitor", ordinal=2)
    public Competitor getCompetitor();

    @Dimension(messageKey="Regatta", ordinal=4)
    String getRegattaName();
    
    @Dimension(messageKey="RelativeScoreInPercent", ordinal=6)
    public ClusterDTO getPercentageClusterForRelativeScore();

    @Dimension(messageKey="WindSpeedInBeaufort", ordinal=7)
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

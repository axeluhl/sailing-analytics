package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.Tack;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;

public interface HasRaceOfCompetitorContext {
    
    @Connector(scanForStatistics=false)
    public HasTrackedRaceContext getTrackedRaceContext();
    
    @Connector(messageKey="Competitor")
    public Competitor getCompetitor();
    
    @Dimension(messageKey="CompetitorSearchTag", ordinal=11) // TODO Clean me: Move Dimension to Competitor when possible
    public String getCompetitorSearchTag();
    
    @Dimension(messageKey="TackAtStart", ordinal=12)
    public Tack getTackAtStart() throws NoWindException;
    
    @Statistic(messageKey="DistanceAtStart", resultDecimals=2, ordinal=0)
    public Distance getDistanceToStartLineAtStart();

    @Statistic(messageKey="DistanceToStarboardSideAtStart", resultDecimals=2, ordinal=1)
    public Double getNormalizedDistanceToStarboardSideAtStart();
    
//    @Statistic(messageKey="StartAdvantage", resultDecimals=2, ordinal=2)
//    public Double getNormalizedDistanceToFavoredEndAtStart();
    
    @Connector(messageKey="SpeedWhenStarting", ordinal=2)
    public Speed getSpeedWhenStarting();
    
    @Connector(messageKey="SpeedTenSecondsBeforeStart", ordinal=3)
    public Speed getSpeedTenSecondsBeforeStart();
    
    @Connector(messageKey="SpeedTenSecondsAfterStart", ordinal=4)
    public Speed getSpeedTenSecondsAfterStart();
    
    @Statistic(messageKey="RankThirtySecondsAfterStart", resultDecimals=2, ordinal=5)
    public Double getRankThirtySecondsAfterStart();
    
    @Statistic(messageKey="RankAfterHalfOfTheFirstLeg", resultDecimals=2, ordinal=6)
    public Double getRankAfterHalfOfTheFirstLeg();
    
    @Statistic(messageKey="RankAtFirstMark", resultDecimals=2, ordinal=6)
    public Double getRankAtFirstMark();
    
    @Statistic(messageKey="RankGainsOrLossesBetweenFirstMarkAndFinish", resultDecimals=2, ordinal=7)
    public Double getRankGainsOrLossesBetweenFirstMarkAndFinish();
    
    @Statistic(messageKey="NumberOfManeuvers", resultDecimals=0, ordinal=8)
    public int getNumberOfManeuvers();

    @Statistic(messageKey="NumberOfTacks", resultDecimals=2, ordinal=9)
    public int getNumberOfTacks();

    @Statistic(messageKey="NumberOfJibes", resultDecimals=2, ordinal=10)
    public int getNumberOfJibes();

    @Statistic(messageKey="NumberOfPenaltyCircles", resultDecimals=2, ordinal=11)
    public int getNumberOfPenaltyCircles();
    
}

package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Speed;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Statistic;
import com.sap.sse.datamining.annotations.data.Unit;

public interface HasRaceOfCompetitorContext {
    
    @Connector(scanForStatistics=false)
    public HasTrackedRaceContext getTrackedRaceContext();
    
    @Connector(messageKey="Competitor")
    public Competitor getCompetitor();
    
    @Statistic(messageKey="DistanceAtStart", resultUnit=Unit.None, resultDecimals=2, ordinal=0)
    public Distance getDistanceToStartLineAtStart();

    @Statistic(messageKey="DistanceToStarboardSideAtStart", resultDecimals=2, ordinal=1)
    public Double getNormalizedDistanceToStarboardSideAtStart();
    
    @Connector(messageKey="SpeedAtStart", ordinal=2)
    public Speed getSpeedAtStart();
    
    @Connector(messageKey="SpeedTenSecondsBeforeStart", ordinal=3)
    public Speed getSpeedTenSecondsBeforeStart();
    
    @Statistic(messageKey="RankAtFirstMark", resultDecimals=2, ordinal=4)
    public Double getRankAtFirstMark();

    @Statistic(messageKey="NumberOfTacks", resultDecimals=2, ordinal=5)
    public Double getNumberOfTacks();

    @Statistic(messageKey="NumberOfJibes", resultDecimals=2, ordinal=6)
    public Double getNumberOfJibes();

    @Statistic(messageKey="NumberOfPenaltyCircles", resultDecimals=2, ordinal=7)
    public Double getNumberOfPenaltyCircles();
    
    @Statistic(messageKey="RankGainsOrLossesBetweenFirstMarkAndFinish", resultDecimals=2, ordinal=8)
    public Double getRankGainsOrLossesBetweenFirstMarkAndFinish();
    
}

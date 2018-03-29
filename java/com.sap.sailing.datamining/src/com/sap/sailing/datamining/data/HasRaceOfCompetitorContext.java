package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.Tack;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;
import com.sap.sse.datamining.shared.impl.dto.ClusterDTO;

public interface HasRaceOfCompetitorContext {
    
    @Connector(scanForStatistics=false)
    public HasTrackedRaceContext getTrackedRaceContext();
    
    @Connector(messageKey="Competitor")
    @Statistic(messageKey="")
    public Competitor getCompetitor();
    
    @Dimension(messageKey="TackAtStart", ordinal=12)
    public Tack getTackAtStart() throws NoWindException;
    
    @Dimension(messageKey="DistanceToStarboardSideAtStart", ordinal=13)
    public ClusterDTO getPercentageClusterForDistanceToStarboardSideAtStart();
    
    @Dimension(messageKey="RelativeScoreInPercent", ordinal=14)
    public ClusterDTO getPercentageClusterForRelativeScore();
    
    @Statistic(messageKey="DistanceAtStart", resultDecimals=2, ordinal=0)
    public Distance getDistanceToStartLineAtStart();

    @Statistic(messageKey="DistanceToStarboardSideAtStart", resultDecimals=2, ordinal=1)
    public Double getNormalizedDistanceToStarboardSideAtStart();
    
    @Statistic(messageKey="DistanceToStarboardSideAtStartVsRankAtFirstMark", resultDecimals=2, ordinal=1)
    public Pair<Double, Double> getNormalizedDistanceToStarboardSideAtStartVsRankAtFirstMark();
    
    @Statistic(messageKey="WindwardDistanceToAdvantageousEndOfLine", resultDecimals=2, ordinal=2)
    public Distance getWindwardDistanceToAdvantageousLineEndAtStart();
    
    @Connector(messageKey="SpeedWhenStarting", ordinal=3)
    public Speed getSpeedWhenStarting();
    
    @Connector(messageKey="SpeedTenSecondsBeforeStart", ordinal=4)
    public Speed getSpeedTenSecondsBeforeStart();
    
    @Connector(messageKey="SpeedTenSecondsAfterStart", ordinal=5)
    public Speed getSpeedTenSecondsAfterStart();
    
    @Statistic(messageKey="RankThirtySecondsAfterStart", resultDecimals=2, ordinal=6)
    public Double getRankThirtySecondsAfterStart();
    
    @Statistic(messageKey="RankAfterHalfOfTheFirstLeg", resultDecimals=2, ordinal=7)
    public Double getRankAfterHalfOfTheFirstLeg();
    
    @Statistic(messageKey="RankAtFirstMark", resultDecimals=2, ordinal=8)
    public Double getRankAtFirstMark();
    
    @Statistic(messageKey="RankGainsOrLossesBetweenFirstMarkAndFinish", resultDecimals=2, ordinal=9)
    public Double getRankGainsOrLossesBetweenFirstMarkAndFinish();
    
    @Statistic(messageKey="NumberOfManeuvers", resultDecimals=0, ordinal=10)
    public int getNumberOfManeuvers();

    @Statistic(messageKey="NumberOfTacks", resultDecimals=0, ordinal=11)
    public int getNumberOfTacks();

    @Statistic(messageKey="NumberOfJibes", resultDecimals=0, ordinal=12)
    public int getNumberOfJibes();

    @Statistic(messageKey="NumberOfPenaltyCircles", resultDecimals=0, ordinal=13)
    public int getNumberOfPenaltyCircles();
    
    @Statistic(messageKey="DistanceTraveled", resultDecimals=1)
    public Distance getDistanceTraveled();
    
}

package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Tack;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Speed;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;
import com.sap.sse.datamining.shared.impl.dto.ClusterDTO;

public interface HasRaceOfCompetitorContext {
    
    @Connector(scanForStatistics=false)
    HasTrackedRaceContext getTrackedRaceContext();
    
    @Connector(messageKey="Competitor")
    @Statistic(messageKey="Competitor")
    Competitor getCompetitor();
    
    @Dimension(messageKey="TackAtStart", ordinal=12)
    Tack getTackAtStart() throws NoWindException;
    
    @Dimension(messageKey="DistanceToStarboardSideAtStartOfCompetitor", ordinal=13)
    ClusterDTO getPercentageClusterForDistanceToStarboardSideAtStart();
    
    @Dimension(messageKey="RelativeScoreInPercent", ordinal=14)
    ClusterDTO getPercentageClusterForRelativeScore();
    
    @Statistic(messageKey="DistanceAtStart", resultDecimals=2, ordinal=0)
    Distance getDistanceToStartLineAtStart();

    @Statistic(messageKey="DistanceToStarboardSideAtStartOfCompetitor", resultDecimals=2, ordinal=1)
    Double getNormalizedDistanceToStarboardSideAtStartOfCompetitor();
    
    @Statistic(messageKey="DistanceToStarboardSideAtStartOfCompetitorVsRankAtFirstMark", resultDecimals=2, ordinal=1)
    Pair<Double, Double> getNormalizedDistanceToStarboardSideAtStartOfCompetitorVsRankAtFirstMark();
    
    @Statistic(messageKey="WindwardDistanceToAdvantageousEndOfLineAtStartOfRace", resultDecimals=2, ordinal=2)
    Distance getWindwardDistanceToAdvantageousLineEndAtStartofRace();
    
    @Statistic(messageKey="WindwardDistanceToAdvantageousEndOfLineAtStartOfCompetitor", resultDecimals=2, ordinal=2)
    Distance getWindwardDistanceToAdvantageousLineEndAtStartofCompetitor();
    
    @Connector(messageKey="SpeedWhenStarting", ordinal=3)
    Speed getSpeedWhenStarting();
    
    @Connector(messageKey="SpeedTenSecondsBeforeStart", ordinal=4)
    Speed getSpeedTenSecondsBeforeStart();
    
    @Connector(messageKey="SpeedTenSecondsAfterStart", ordinal=5)
    Speed getSpeedTenSecondsAfterStartOfRace();
    
    @Statistic(messageKey="RankThirtySecondsAfterStart", resultDecimals=2, ordinal=6)
    Double getRankThirtySecondsAfterStartOfRace();
    
    @Statistic(messageKey="RankAfterHalfOfTheFirstLeg", resultDecimals=2, ordinal=7)
    Double getRankAfterHalfOfTheFirstLeg();
    
    @Statistic(messageKey="RankAtFirstMark", resultDecimals=2, ordinal=8)
    Double getRankAtFirstMark();
    
    @Statistic(messageKey="RankGainsOrLossesBetweenFirstMarkAndFinish", resultDecimals=2, ordinal=9)
    Double getRankGainsOrLossesBetweenFirstMarkAndFinish();
    
    @Statistic(messageKey="NumberOfManeuvers", resultDecimals=0, ordinal=10)
    int getNumberOfManeuvers();

    @Statistic(messageKey="NumberOfTacks", resultDecimals=0, ordinal=11)
    int getNumberOfTacks();

    @Statistic(messageKey="NumberOfJibes", resultDecimals=0, ordinal=12)
    int getNumberOfJibes();

    @Statistic(messageKey="NumberOfPenaltyCircles", resultDecimals=0, ordinal=13)
    int getNumberOfPenaltyCircles();
    
    @Statistic(messageKey="DistanceTraveled", resultDecimals=1)
    Distance getDistanceTraveled();
    
    @Statistic(messageKey="LineLengthAtStart", resultDecimals=1)
    Distance getLineLengthAtStart();

    @Statistic(messageKey="AbsoluteWindwardDistanceToStarboardSideAtStartOfCompetitor", resultDecimals=2)
    Distance getAbsoluteWindwardDistanceToStarboardSideAtStartOfCompetitor();
    
    @Statistic(messageKey="DistanceToStarboardSideAtStartOfCompetitorVsFinalRank", resultDecimals=2)
    Pair<Double, Double> getRelativeDistanceToStarboardSideAtStartOfCompetitorVsFinalRank();
    
    @Statistic(messageKey="WindwardDistanceToAdvantageousEndOfLineAtStartOfRaceVsRelativeDistanceToAdvantageousEndOfLine", resultDecimals=2)
    Pair<Double, Double> getWindwardDistanceToAdvantageousEndOfLineAtStartOfRaceVsRelativeDistanceToAdvantageousEndOfLineAtStartOfRace();
    
    @Statistic(messageKey="RelativeDistanceToAdvantageousEndOfLineAtStartOfRace", resultDecimals=2)
    Double getRelativeDistanceToAdvantageousEndOfLineAtStartOfRace();
    
    @Statistic(messageKey="RaceDuration")
    Duration getDuration();
    
}

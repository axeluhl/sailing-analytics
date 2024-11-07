package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Boat;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;
import com.sap.sse.datamining.shared.impl.dto.ClusterDTO;

/**
 * A "slice" of a competitor's tracked leg; slicing may happen in the retriever, perhaps influenced by some
 * configuration parameters, e.g., based on windward distance sailed, time spent, or rhumb line distance sailed.
 * A full leg is a special case of this.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public interface AbstractHasTrackedLegSliceOfCompetitorContext extends HasWindOnTrackedLeg {
    @Connector(scanForStatistics=false)
    public HasTrackedLegContext getTrackedLegContext();
    
    public TrackedLegOfCompetitor getTrackedLegOfCompetitor();
    
    @Dimension(messageKey="RelativeScoreInRaceInPercent", ordinal=12)
    public ClusterDTO getPercentageClusterForRelativeScoreInRace();
    
    @Connector(messageKey="Boat")
    public Boat getBoat();
    
    @Connector(messageKey="Competitor")
    public Competitor getCompetitor();
    
    @Statistic(messageKey="DistanceTraveled", resultDecimals=2, ordinal=0)
    public Distance getDistanceTraveled();
    
    @Statistic(messageKey="SpeedAverage", resultDecimals=2, ordinal=0)
    public Double getSpeedAverageInKnots();
    
    @Statistic(messageKey="RankGainsOrLosses", resultDecimals=2, ordinal=1)
    public Integer getRankGainsOrLosses();
    
    @Statistic(messageKey="RelativeScore", resultDecimals=2, ordinal=2)
    public Double getRelativeRank();
    
    @Statistic(messageKey="AbsoluteRank", resultDecimals=0, ordinal=3)
    public Integer getRankAtSliceFinish();
    
    @Statistic(messageKey="AbsoluteRankAfterFirstQuarter", resultDecimals=0, ordinal=4)
    public Integer getRankAfterFirstQuarter();
    
    @Statistic(messageKey="AbsoluteRankAfterSecondQuarter", resultDecimals=0, ordinal=5)
    public Integer getRankAfterSecondQuarter();
    
    @Statistic(messageKey="AbsoluteRankAfterThirdQuarter", resultDecimals=0, ordinal=6)
    public Integer getRankAfterThirdQuarter();
    
    @Statistic(messageKey="AbsoluteRankAverageAcrossFirstThreeQuarters", resultDecimals=2, ordinal=7)
    public Double getRankAverageAcrossFirstThreeQuarters();
    
    @Statistic(messageKey="AbsoluteRankRhumbLineBasedAfterFirstQuarter", resultDecimals=0, ordinal=8)
    public Integer getRankRhumbLineBasedAfterFirstQuarter();
    
    @Statistic(messageKey="AbsoluteRankRhumbLineBasedAfterSecondQuarter", resultDecimals=0, ordinal=9)
    public Integer getRankRhumbLineBasedAfterSecondQuarter();
    
    @Statistic(messageKey="AbsoluteRankRhumbLineBasedAfterThirdQuarter", resultDecimals=0, ordinal=10)
    public Integer getRankRhumbLineBasedAfterThirdQuarter();
    
    @Statistic(messageKey="AbsoluteRankRhumbLineBasedAverageAcrossFirstThreeQuarters", resultDecimals=2, ordinal=11)
    public Double getRankRhumbLineBasedAverageAcrossFirstThreeQuarters();
    
    @Statistic(messageKey="AbsoluteRankPredictionErrorAfterFirstQuarter", resultDecimals=2, ordinal=12)
    public Double getRankPredictionErrorAfterFirstQuarter();
    
    @Statistic(messageKey="AbsoluteRankPredictionErrorAfterSecondQuarter", resultDecimals=2, ordinal=13)
    public Double getRankPredictionErrorAfterSecondQuarter();
    
    @Statistic(messageKey="AbsoluteRankPredictionErrorAfterThirdQuarter", resultDecimals=2, ordinal=14)
    public Double getRankPredictionErrorAfterThirdQuarter();
    
    @Statistic(messageKey="AbsoluteRankPredictionErrorAcrossFirstThreeQuarters", resultDecimals=2, ordinal=15)
    public Double getRankPredictionErrorAcrossFirstThreeQuarters();
    
    @Statistic(messageKey="AbsoluteRankPredictionErrorRhumbLineBasedAfterFirstQuarter", resultDecimals=2, ordinal=16)
    public Double getRankPredictionErrorRhumbLineBasedAfterFirstQuarter();
    
    @Statistic(messageKey="AbsoluteRankPredictionErrorRhumbLineBasedAfterSecondQuarter", resultDecimals=2, ordinal=17)
    public Double getRankPredictionErrorRhumbLineBasedAfterSecondQuarter();
    
    @Statistic(messageKey="AbsoluteRankPredictionErrorRhumbLineBasedAfterThirdQuarter", resultDecimals=2, ordinal=18)
    public Double getRankPredictionErrorRhumbLineBasedAfterThirdQuarter();
    
    @Statistic(messageKey="AbsoluteRankPredictionErrorRhumbLineBasedAcrossFirstThreeQuarters", resultDecimals=2, ordinal=19)
    public Double getRankPredictionErrorRhumbLineBasedAcrossFirstThreeQuarters();
    
    @Statistic(messageKey="TimeSpentInSeconds", resultDecimals=0, ordinal=4)
    public Long getTimeTakenInSeconds();

    @Statistic(messageKey="timeSpentFoiling", resultDecimals=1)
    Duration getTimeSpentFoiling();

    @Statistic(messageKey="FoilingDistance", resultDecimals=1)
    Distance getDistanceSpentFoiling();
    
    @Statistic(messageKey="NumberOfManeuvers", resultDecimals=0)
    public Integer getNumberOfManeuvers();
    
    @Statistic(messageKey="NumberOfJibes", resultDecimals=0)
    public Integer getNumberOfJibes();
    
    @Statistic(messageKey="NumberOfTacks", resultDecimals=0)
    public Integer getNumberOfTacks();
    
    @Statistic(messageKey="VMG", resultDecimals=2)
    public Speed getVelocityMadeGood();
    
    @Statistic(messageKey="RatioDurationLongVsShortTack", resultDecimals=2)
    public double getRatioDurationLongVsShortTack();
    
    @Statistic(messageKey="RatioDistanceLongVsShortTack", resultDecimals=2)
    public double getRatioDistanceLongVsShortTack();
}
package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;
import com.sap.sse.datamining.shared.impl.dto.ClusterDTO;

public interface HasTrackedLegOfCompetitorContext extends HasWindOnTrackedLeg {
    @Connector(scanForStatistics=false)
    public HasTrackedLegContext getTrackedLegContext();
    
    public TrackedLegOfCompetitor getTrackedLegOfCompetitor();
    
    @Dimension(messageKey="RelativeScoreInRaceInPercent", ordinal=12)
    public ClusterDTO getPercentageClusterForRelativeScoreInRace();
    
    @Dimension(messageKey="SailID")
    public String getSailID();
    
    @Connector(messageKey="Competitor")
    public Competitor getCompetitor();
    
    @Statistic(messageKey="DistanceTraveled", resultDecimals=2, ordinal=0)
    public Distance getDistanceTraveled();
    
    @Statistic(messageKey="SpeedAverage", resultDecimals=2, ordinal=0)
    public Double getSpeedAverage();
    
    @Statistic(messageKey="SpeedAverageVsDistanceTraveled", resultDecimals=2, ordinal=0)
    public Pair<Double, Double> getSpeedAverageVsDistanceTraveled();
    
    @Statistic(messageKey="RankGainsOrLosses", resultDecimals=2, ordinal=1)
    public Integer getRankGainsOrLosses();
    
    @Statistic(messageKey="RelativeScore", resultDecimals=2, ordinal=2)
    public Double getRelativeRank();
    
    @Statistic(messageKey="AbsoluteRank", resultDecimals=2, ordinal=3)
    public Integer getRankAtFinish();
    
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
}
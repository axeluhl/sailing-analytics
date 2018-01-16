package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;

/**
 * Contains statistics for analysis of computation correctness of maneuver boundaries.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface HasManeuverBoundariesContext {
    
    @Connector(scanForStatistics = false)
    HasRaceOfCompetitorContext getRaceOfCompetitorContext();

    // FIXME due to UI bug there is at least one dimension per fact required
    @Dimension(messageKey = "ManeuverDirection", ordinal = 3)
    NauticalSide getToSide();
    
    @Dimension(messageKey = "ManeuverType", ordinal = 4)
    ManeuverType getManeuverType();

    @Statistic(messageKey = "ManeuverStartSpeedDeviationRatioFromAvg", ordinal = 1, resultDecimals = 4)
    Double getManeuverStartSpeedDeviationRatioFromAvgStatistic();
    
    @Statistic(messageKey = "ManeuverStartCogDeviationFromAvgInDegrees", ordinal = 2, resultDecimals = 4)
    Double getManeuverStartCogDeviationFromAvgInDegreesStatistic();
    
    @Statistic(messageKey = "ManeuverEndSpeedDeviationRatioFromAvg", ordinal = 3, resultDecimals = 4)
    Double getManeuverEndSpeedDeviationRatioFromAvgStatistic();
    
    @Statistic(messageKey = "ManeuverEndCogDeviationFromAvgInDegrees", ordinal = 4, resultDecimals = 4)
    Double getManeuverEndCogDeviationFromAvgInDegreesStatistic();
    
    @Statistic(messageKey = "DurationToNextManeuverInSeconds", ordinal = 5, resultDecimals = 4)
    Double getDurationToNextManeuverInSecondsStatistic();
    
    @Statistic(messageKey = "DurationFromPreviousManeuverInSeconds", ordinal = 6, resultDecimals = 4)
    Double getDurationFromPreviousManeuverInSecondsStatistic();
    
    @Statistic(messageKey = "NextManeuverAtLeastOneSecondInFront", ordinal = 7)
    boolean isNextManeuverAtLeastOneSecondInFront();
    
    @Statistic(messageKey = "PreviousManeuverAtLeastOneSecondBehind", ordinal = 8)
    boolean isPreviousManeuverAtLeastOneSecondBehind();

}

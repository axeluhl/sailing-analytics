package com.sap.sailing.datamining.data;

import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Statistic;

public interface HasManeuverSpeedDetailsContext {
    
    @Connector(scanForStatistics=false)
    HasManeuverContext getManeuverContext();
    
    @Statistic(messageKey="SpeedSlope", ordinal=8, resultDecimals=4)
    ManeuverSpeedDetailsStatistic getSpeedSlopeStatistic();
    
    @Statistic(messageKey="RatioToInitialSpeed", ordinal=1, resultDecimals=4)
    ManeuverSpeedDetailsStatistic getRatioToInitialSpeedStatistic();

    @Statistic(messageKey="RatioToPreviousTWA", ordinal=5, resultDecimals=4)
    ManeuverSpeedDetailsStatistic getRatioToPreviousTWAStatistic();
    
    @Statistic(messageKey="LowestRatioToInitialSpeed", ordinal=2, resultDecimals=4)
    Double getLowestRatioToInitialSpeedStatistic();
    
    @Statistic(messageKey="HighestRatioToInitialSpeed", ordinal=3, resultDecimals=4)
    Double getHighestRatioToInitialSpeedStatistic();
    
    @Statistic(messageKey="HighestRatioToInitialSpeedMinusLowest", ordinal=4, resultDecimals=4)
    Double getHighestRatioToInitialSpeedMinusLowestStatistic();
    
    @Statistic(messageKey="EnteringManeuverSpeedMinusExitingSpeed", ordinal=7, resultDecimals=4)
    Double getEnteringManeuverSpeedMinusExitingSpeedStatistic();
    
    @Statistic(messageKey="RatioBetweenInitialAndFinalManeuverSpeed", ordinal=6, resultDecimals=4)
    Double getRatioBetweenInitialAndFinalManeuverSpeedStatistic();
    
}

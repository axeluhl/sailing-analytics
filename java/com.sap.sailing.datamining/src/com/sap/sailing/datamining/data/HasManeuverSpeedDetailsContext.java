package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;

public interface HasManeuverSpeedDetailsContext {
    
    //This NauticalSide may differ from NauticalSide in HasManeuverContext due to different bearing approximation strategies
    //The approximation strategy for this dimension is also used for determination of maneuver speed details.
    @Dimension(messageKey="ManeuverDirection")
    NauticalSide getToSide();
    
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

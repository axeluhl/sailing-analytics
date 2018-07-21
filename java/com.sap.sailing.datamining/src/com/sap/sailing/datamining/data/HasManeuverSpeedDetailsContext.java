package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;

/**
 * Contains statistics for speed trend analysis within maneuvers.
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface HasManeuverSpeedDetailsContext {

    // FIXME due to UI bug there is at least one dimension per fact required
    @Dimension(messageKey = "ManeuverDirection")
    NauticalSide getToSide();

    @Connector(scanForStatistics = false)
    HasManeuverContext getManeuverContext();

    @Statistic(messageKey = "SpeedSlope", ordinal = 6, resultDecimals = 4)
    ManeuverSpeedDetailsStatistic getSpeedSlopeStatistic();

    @Statistic(messageKey = "RatioToEnteringSpeed", ordinal = 1, resultDecimals = 4)
    ManeuverSpeedDetailsStatistic getRatioToEnteringSpeedStatistic();

    @Statistic(messageKey = "RatioToPreviousTWASpeed", ordinal = 5, resultDecimals = 4)
    ManeuverSpeedDetailsStatistic getRatioToPreviousTWASpeedStatistic();

    @Statistic(messageKey = "LowestRatioToEnteringSpeed", ordinal = 2, resultDecimals = 4)
    Double getLowestRatioToEnteringSpeedStatistic();
    
    @Statistic(messageKey = "AbsTwaAtLowestRatioToEnteringSpeed", ordinal = 5, resultDecimals = 4)
    Double getAbsTwaAtLowestRatioToEnteringSpeedStatistic();

    @Statistic(messageKey = "HighestRatioToEnteringSpeed", ordinal = 3, resultDecimals = 4)
    Double getHighestRatioToEnteringSpeedStatistic();
    
    @Statistic(messageKey = "AbsTwaAtHighestRatioToEnteringSpeed", ordinal = 6, resultDecimals = 4)
    Double getAbsTwaAtHighestRatioToEnteringSpeedStatistic();

    @Statistic(messageKey = "HighestRatioToEnteringSpeedMinusLowest", ordinal = 4, resultDecimals = 4)
    Double getHighestRatioToEnteringSpeedMinusLowestStatistic();

}

package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;
import com.sap.sse.datamining.shared.impl.dto.ClusterDTO;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public interface HasCompleteManeuverCurveWithEstimationDataContext {

    @Connector(scanForStatistics = false)
    HasRaceOfCompetitorContext getRaceOfCompetitorContext();

    @Connector
    CompleteManeuverCurveWithEstimationData getCompleteManeuverCurveWithEstimationData();

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

    @Dimension(messageKey = "NextManeuverAtLeastOneSecondInFront", ordinal = 7)
    boolean isNextManeuverAtLeastOneSecondInFront();

    @Dimension(messageKey = "PreviousManeuverAtLeastOneSecondBehind", ordinal = 8)
    boolean isPreviousManeuverAtLeastOneSecondBehind();

    @Dimension(messageKey = "JibingCount")
    ClusterDTO getJibingCount();

    @Dimension(messageKey = "TackingCount")
    ClusterDTO getTackingCount();

    @Statistic(messageKey = "AbsTwaAtMaxTurningRate")
    Bearing getAbsTwaAtMaxTurningRate();

    @Statistic(messageKey = "AbsTwaAtLowestSpeed")
    Bearing getAbsTwaAtLowestSpeed();

    @Statistic(messageKey = "AbsTwaAtHighestSpeed")
    Bearing getAbsTwaAtHighestSpeed();

}

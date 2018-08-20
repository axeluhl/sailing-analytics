package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NauticalSide;
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

    @Dimension(messageKey = "ToSide")
    NauticalSide getToSide();

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
    double getAbsTwaAtMaxTurningRate();

    @Statistic(messageKey = "AbsTwaAtLowestSpeed")
    double getAbsTwaAtLowestSpeed();

    @Statistic(messageKey = "AbsTwaAtHighestSpeed")
    double getAbsTwaAtHighestSpeed();

    @Statistic(messageKey = "AbsTwaAtManeuverMiddle")
    double getAbsTwaAtManeuverMiddle();

    @Statistic(messageKey = "GpsSamplingRate")
    double getGpsSamplingRate();

    @Statistic(messageKey = "ManeuverEnteringAbsTWA")
    Double getEnteringAbsTWA();

    @Statistic(messageKey = "ManeuverExitingAbsTWA")
    Double getExitingAbsTWA();

    @Dimension(messageKey = "TypeOfPreviousManeuver")
    ManeuverType getTypeOfPreviousManeuver();

    @Dimension(messageKey = "TypeOfNextManeuver")
    ManeuverType getTypeOfNextManeuver();

    @Statistic(messageKey = "AbsoluteDirectionChange", resultDecimals = 2)
    Double getAbsoluteDirectionChangeInDegrees();

    @Statistic(messageKey = "RatioBetweenDistanceSailedWithAndWithoutManeuver", resultDecimals = 4)
    double getRatioBetweenDistanceSailedWithAndWithoutManeuver();

    @Statistic(messageKey = "DurationLostByManeuver", resultDecimals = 4)
    double getDurationLostByManeuver();

    @Statistic(messageKey = "DurationLostByManeuverTowardMiddleAngleProjection", resultDecimals = 4)
    double getDurationLostByManeuverTowardMiddleAngleProjection();

    @Statistic(messageKey = "RatioBetweenDistanceSailedTowardMiddleAngleProjectionWithAndWithoutManeuver", resultDecimals = 4)
    double getRatioBetweenDistanceSailedTowardMiddleAngleProjectionWithAndWithoutManeuver();

    @Statistic(messageKey = "RelativeBearingToNextMarkBeforeManeuver", resultDecimals = 1)
    Double getRelativeBearingToNextMarkBeforeManeuver();

    @Statistic(messageKey = "RelativeBearingToNextMarkAfterManeuver", resultDecimals = 1)
    Double getRelativeBearingToNextMarkAfterManeuver();
    
    @Statistic(messageKey = "AbsRelativeBearingToNextMarkBeforeManeuver", resultDecimals = 1)
    Double getAbsRelativeBearingToNextMarkBeforeManeuver();

    @Statistic(messageKey = "AbsRelativeBearingToNextMarkAfterManeuver", resultDecimals = 1)
    Double getAbsRelativeBearingToNextMarkAfterManeuver();

    @Statistic(messageKey = "ManeuverEnteringSpeedInKnots", resultDecimals = 2)
    Double getManeuverEnteringSpeed();

    @Statistic(messageKey = "ManeuverExitingSpeedInKnots", resultDecimals = 2)
    Double getManeuverExitingSpeed();

}

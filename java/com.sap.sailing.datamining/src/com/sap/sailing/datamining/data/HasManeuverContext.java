package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.Tack;
import com.sap.sse.common.TimePoint;
import com.sap.sse.datamining.annotations.Connector;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;

public interface HasManeuverContext extends HasWindOnTrackedLeg, HasManeuver, HasTrackedLegOfCompetitor {
    @Connector(scanForStatistics = false)
    HasTrackedLegOfCompetitorContext getTrackedLegOfCompetitorContext();

    @Dimension(messageKey = "ManeuverType", ordinal = 12)
    ManeuverType getManeuverType();

    @Dimension(messageKey = "ToSide", ordinal = 16)
    NauticalSide getToSide();

    @Dimension(messageKey = "TackBeforeManeuver")
    Tack getTackBeforeManeuver();

    @Statistic(messageKey = "AbsoluteDirectionChange", resultDecimals = 2, ordinal = 3)
    Double getAbsoluteDirectionChangeInDegrees();

    @Statistic(messageKey = "ManeuverLoss", resultDecimals = 0, ordinal = 4)
    Distance getManeuverLoss();

    @Statistic(messageKey = "AbsTWAAtManeuverClimax", ordinal = 7)
    Double getAbsTWAAtManeuverClimax();

    @Statistic(messageKey = "ManeuverEnteringAbsTWA", ordinal = 5)
    Double getEnteringAbsTWA();

    @Statistic(messageKey = "ManeuverExitingAbsTWA", ordinal = 6)
    Double getExitingAbsTWA();

    @Statistic(messageKey = "ManeuverDurationInSeconds", ordinal = 8, resultDecimals = 2)
    Double getManeuverDurationInSeconds();

    @Statistic(messageKey = "EnteringManeuverSpeedMinusExitingSpeed", ordinal = 19, resultDecimals = 4)
    Double getEnteringManeuverSpeedMinusExitingSpeed();

    @Statistic(messageKey = "RatioBetweenManeuverEnteringAndExitingSpeed", ordinal = 20, resultDecimals = 4)
    Double getRatioBetweenManeuverEnteringAndExitingSpeed();

    @Statistic(messageKey = "ManeuverEnteringSpeedInKnots", ordinal = 17, resultDecimals = 2)
    Double getManeuverEnteringSpeed();

    @Statistic(messageKey = "ManeuverExitingSpeedInKnots", ordinal = 18, resultDecimals = 2)
    Double getManeuverExitingSpeed();
    
    TimePoint getTimePointBeforeForAnalysis();

    TimePoint getTimePointAfterForAnalysis();

    double getDirectionChangeInDegreesForAnalysis();

}

package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.tracking.GPSFix;
import com.sap.sse.common.TimePoint;
import com.sap.sse.datamining.annotations.Dimension;
import com.sap.sse.datamining.annotations.Statistic;

public interface Maneuver extends GPSFix {
    ManeuverType getType();

    @Dimension(messageKey = "Tack", ordinal = 13)
    Tack getNewTack();

    SpeedWithBearing getSpeedWithBearingBefore();

    SpeedWithBearing getSpeedWithBearingAfter();

    @Statistic(messageKey = "DirectionChange", resultDecimals = 2, ordinal = 2)
    double getDirectionChangeInDegrees();

    Distance getManeuverLoss();

    TimePoint getTimePointBefore();

    TimePoint getTimePointAfter();

}

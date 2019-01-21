package com.sap.sailing.windestimation.data.transformer;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sse.common.Bearing;
import com.sap.sse.common.Duration;
import com.sap.sse.common.Speed;
import com.sap.sse.common.TimePoint;

public interface ConvertableToManeuverForEstimation {

    Double getCourseChangeInDegreesWithinTurningSectionOfPreviousManeuver();

    Double getCourseChangeInDegreesWithinTurningSectionOfNextManeuver();

    double getCourseChangeInDegreesWithinTurningSection();

    double getCourseChangeInDegrees();

    boolean isMarkPassing();

    SpeedWithBearing getSpeedWithBearingBefore();

    SpeedWithBearing getSpeedWithBearingAfter();

    Duration getLongestIntervalBetweenTwoFixes();

    Duration getIntervalBetweenFirstFixOfCurveAndPreviousFix();

    Duration getIntervalBetweenLastFixOfCurveAndNextFix();

    Duration getDurationFromPreviousManeuverEndToManeuverStart();

    Duration getDurationFromManeuverEndToNextManeuverStart();

    Speed getLowestSpeed();

    Speed getHighestSpeedWithinTurningSection();

    SpeedWithBearing getSpeedWithBearingBeforeWithinTurningSection();

    SpeedWithBearing getSpeedWithBearingAfterWithinTurningSection();

    Double getTargetTackAngleInDegrees();

    Double getTargetJibeAngleInDegrees();

    boolean hasMarkPassingData();

    double getMaxTurningRateInDegreesPerSecond();

    Bearing getMiddleCourse();

    Position getPosition();

    TimePoint getTimePoint();

}

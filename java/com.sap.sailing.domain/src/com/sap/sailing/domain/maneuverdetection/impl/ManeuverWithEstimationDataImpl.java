package com.sap.sailing.domain.maneuverdetection.impl;

import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.Wind;
import com.sap.sailing.domain.maneuverdetection.ManeuverWithEstimationData;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sse.common.Duration;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverWithEstimationDataImpl implements ManeuverWithEstimationData {
    private final Maneuver maneuver;
    private final Wind wind;
    private final SpeedWithBearing highestSpeedWithinMainCurve;
    private final SpeedWithBearing lowestSpeedWithinMainCurve;
    private final SpeedWithBearing averageSpeedWithBearingBefore;
    private final SpeedWithBearing averageSpeedWithBearingAfter;
    private final Duration durationFromPreviousManeuverEndToManeuverStart;
    private final Duration durationFromManeuverEndToNextManeuverStart;

    public ManeuverWithEstimationDataImpl(Maneuver maneuver, Wind wind, SpeedWithBearing highestSpeedWithinMainCurve,
            SpeedWithBearing lowestSpeedWithinMainCurve, SpeedWithBearing averageSpeedWithBearingBefore,
            Duration durationFromPreviousManeuverEndToManeuverStart, SpeedWithBearing averageSpeedWithBearingAfter,
            Duration durationFromManeuverEndToNextManeuverStart) {
        this.maneuver = maneuver;
        this.wind = wind;
        this.highestSpeedWithinMainCurve = highestSpeedWithinMainCurve;
        this.lowestSpeedWithinMainCurve = lowestSpeedWithinMainCurve;
        this.averageSpeedWithBearingBefore = averageSpeedWithBearingBefore;
        this.durationFromPreviousManeuverEndToManeuverStart = durationFromPreviousManeuverEndToManeuverStart;
        this.averageSpeedWithBearingAfter = averageSpeedWithBearingAfter;
        this.durationFromManeuverEndToNextManeuverStart = durationFromManeuverEndToNextManeuverStart;
    }

    @Override
    public Maneuver getManeuver() {
        return maneuver;
    }

    @Override
    public Wind getWind() {
        return wind;
    }

    @Override
    public SpeedWithBearing getHighestSpeedWithinMainCurve() {
        return highestSpeedWithinMainCurve;
    }

    @Override
    public SpeedWithBearing getLowestSpeedWithinMainCurve() {
        return lowestSpeedWithinMainCurve;
    }

    @Override
    public SpeedWithBearing getAverageSpeedWithBearingBefore() {
        return averageSpeedWithBearingBefore;
    }

    @Override
    public Duration getDurationFromPreviousManeuverEndToManeuverStart() {
        return durationFromPreviousManeuverEndToManeuverStart;
    }

    @Override
    public SpeedWithBearing getAverageSpeedWithBearingAfter() {
        return averageSpeedWithBearingAfter;
    }

    @Override
    public Duration getDurationFromManeuverEndToNextManeuverStart() {
        return durationFromManeuverEndToNextManeuverStart;
    }

}

package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasManeuverBoundariesContext;
import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.NauticalSide;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.maneuverdetection.ManeuverWithEstimationData;
import com.sap.sse.common.Duration;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class ManeuverBoundariesWithContext implements HasManeuverBoundariesContext {

    private final ManeuverWithEstimationData maneuverWithEstimationData;
    private HasRaceOfCompetitorContext raceOfCompetitorContext;

    public ManeuverBoundariesWithContext(HasRaceOfCompetitorContext raceOfCompetitorContext, ManeuverWithEstimationData maneuverWithEstimationData) {
        this.raceOfCompetitorContext = raceOfCompetitorContext;
        this.maneuverWithEstimationData = maneuverWithEstimationData;
    }
    
    @Override
    public HasRaceOfCompetitorContext getRaceOfCompetitorContext() {
        return raceOfCompetitorContext;
    }

    @Override
    public NauticalSide getToSide() {
        return maneuverWithEstimationData.getManeuver().getDirectionChangeInDegrees() >= 0 ? NauticalSide.STARBOARD
                : NauticalSide.PORT;
    }
    
    @Override
    public ManeuverType getManeuverType() {
        return maneuverWithEstimationData.getManeuver().getType();
    }

    @Override
    public Double getManeuverBoundarySpeedDeviationRatioFromAvgStatistic() {
        SpeedWithBearing speedWithBearingBeforeManeuver = maneuverWithEstimationData.getManeuver()
                .getManeuverCurveWithStableSpeedAndCourseBoundaries().getSpeedWithBearingBefore();
        SpeedWithBearing averageSpeedWithBearingBeforeManeuver = maneuverWithEstimationData
                .getAverageSpeedWithBearingBefore();
        if (speedWithBearingBeforeManeuver != null && averageSpeedWithBearingBeforeManeuver != null) {
            return averageSpeedWithBearingBeforeManeuver.getKnots() / speedWithBearingBeforeManeuver.getKnots();
        }
        return null;
    }

    @Override
    public Double getManeuverBoundaryCogDeviationRatioFromAvgStatistic() {
        SpeedWithBearing speedWithBearingBeforeManeuver = maneuverWithEstimationData.getManeuver()
                .getManeuverCurveWithStableSpeedAndCourseBoundaries().getSpeedWithBearingBefore();
        SpeedWithBearing averageSpeedWithBearingBeforeManeuver = maneuverWithEstimationData
                .getAverageSpeedWithBearingBefore();
        if (speedWithBearingBeforeManeuver != null && averageSpeedWithBearingBeforeManeuver != null) {
            return averageSpeedWithBearingBeforeManeuver.getBearing().getDegrees()
                    / speedWithBearingBeforeManeuver.getBearing().getDegrees();
        }
        return null;
    }

    @Override
    public Double getDurationToNextManeuverInSecondsStatistic() {
        Duration duration = maneuverWithEstimationData.getDurationFromManeuverEndToNextManeuverStart();
        if (duration != null) {
            return duration.asSeconds();
        }
        return null;
    }

    @Override
    public Double getDurationFromPreviousManeuverInSecondsStatistic() {
        Duration duration = maneuverWithEstimationData.getDurationFromPreviousManeuverEndToManeuverStart();
        if (duration != null) {
            return duration.asSeconds();
        }
        return null;
    }

    public boolean isNextManeuverAtLeastOneSecondInFront() {
        return maneuverWithEstimationData.getDurationFromManeuverEndToNextManeuverStart() != null;
    }

    @Override
    public boolean isPreviousManeuverAtLeastOneSecondBehind() {
        return maneuverWithEstimationData.getDurationFromPreviousManeuverEndToManeuverStart() != null;
    }

}

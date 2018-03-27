package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasCompleteManeuverCurveWithEstimationDataContext;
import com.sap.sailing.datamining.data.HasRaceOfCompetitorContext;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sse.common.Duration;
import com.sap.sse.datamining.shared.impl.dto.ClusterDTO;

/**
 * 
 * @author Vladislav Chumak (D069712)
 *
 */
public class CompleteManeuverCurveWithEstimationDataWithContext
        implements HasCompleteManeuverCurveWithEstimationDataContext {

    private final CompleteManeuverCurveWithEstimationData maneuverWithEstimationData;
    private HasRaceOfCompetitorContext raceOfCompetitorContext;

    public CompleteManeuverCurveWithEstimationDataWithContext(HasRaceOfCompetitorContext raceOfCompetitorContext,
            CompleteManeuverCurveWithEstimationData maneuverWithEstimationData) {
        this.raceOfCompetitorContext = raceOfCompetitorContext;
        this.maneuverWithEstimationData = maneuverWithEstimationData;
    }

    @Override
    public HasRaceOfCompetitorContext getRaceOfCompetitorContext() {
        return raceOfCompetitorContext;
    }

    @Override
    public Double getManeuverStartSpeedDeviationRatioFromAvgStatistic() {
        SpeedWithBearing speedWithBearingBeforeManeuver = maneuverWithEstimationData
                .getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore();
        SpeedWithBearing averageSpeedWithBearingBeforeManeuver = maneuverWithEstimationData
                .getCurveWithUnstableCourseAndSpeed().getAverageSpeedWithBearingBefore();
        if (speedWithBearingBeforeManeuver != null && averageSpeedWithBearingBeforeManeuver != null) {
            if (speedWithBearingBeforeManeuver.getKnots() < averageSpeedWithBearingBeforeManeuver.getKnots()) {
                return averageSpeedWithBearingBeforeManeuver.getKnots() / speedWithBearingBeforeManeuver.getKnots();
            } else {
                return speedWithBearingBeforeManeuver.getKnots() / averageSpeedWithBearingBeforeManeuver.getKnots();
            }
        }
        return null;
    }

    @Override
    public Double getManeuverStartCogDeviationFromAvgInDegreesStatistic() {
        SpeedWithBearing speedWithBearingBeforeManeuver = maneuverWithEstimationData
                .getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore();
        SpeedWithBearing averageSpeedWithBearingBeforeManeuver = maneuverWithEstimationData
                .getCurveWithUnstableCourseAndSpeed().getAverageSpeedWithBearingBefore();
        if (speedWithBearingBeforeManeuver != null && averageSpeedWithBearingBeforeManeuver != null) {
            return Math.abs(averageSpeedWithBearingBeforeManeuver.getBearing()
                    .getDifferenceTo(speedWithBearingBeforeManeuver.getBearing()).getDegrees());
        }
        return null;
    }

    @Override
    public Double getManeuverEndSpeedDeviationRatioFromAvgStatistic() {
        SpeedWithBearing speedWithBearingAfterManeuver = maneuverWithEstimationData.getCurveWithUnstableCourseAndSpeed()
                .getSpeedWithBearingAfter();
        SpeedWithBearing averageSpeedWithBearingAfterManeuver = maneuverWithEstimationData
                .getCurveWithUnstableCourseAndSpeed().getAverageSpeedWithBearingAfter();
        if (speedWithBearingAfterManeuver != null && averageSpeedWithBearingAfterManeuver != null) {
            if (speedWithBearingAfterManeuver.getKnots() < averageSpeedWithBearingAfterManeuver.getKnots()) {
                return averageSpeedWithBearingAfterManeuver.getKnots() / speedWithBearingAfterManeuver.getKnots();
            } else {
                return speedWithBearingAfterManeuver.getKnots() / averageSpeedWithBearingAfterManeuver.getKnots();
            }
        }
        return null;
    }

    @Override
    public Double getManeuverEndCogDeviationFromAvgInDegreesStatistic() {
        SpeedWithBearing speedWithBearingAfterManeuver = maneuverWithEstimationData.getCurveWithUnstableCourseAndSpeed()
                .getSpeedWithBearingAfter();
        SpeedWithBearing averageSpeedWithBearingAfterManeuver = maneuverWithEstimationData
                .getCurveWithUnstableCourseAndSpeed().getAverageSpeedWithBearingAfter();
        if (speedWithBearingAfterManeuver != null && averageSpeedWithBearingAfterManeuver != null) {
            return Math.abs(averageSpeedWithBearingAfterManeuver.getBearing()
                    .getDifferenceTo(speedWithBearingAfterManeuver.getBearing()).getDegrees());
        }
        return null;
    }

    @Override
    public Double getDurationToNextManeuverInSecondsStatistic() {
        Duration duration = maneuverWithEstimationData.getCurveWithUnstableCourseAndSpeed()
                .getDurationFromManeuverEndToNextManeuverStart();
        if (duration != null) {
            return duration.asSeconds();
        }
        return null;
    }

    @Override
    public Double getDurationFromPreviousManeuverInSecondsStatistic() {
        Duration duration = maneuverWithEstimationData.getCurveWithUnstableCourseAndSpeed()
                .getDurationFromPreviousManeuverEndToManeuverStart();
        if (duration != null) {
            return duration.asSeconds();
        }
        return null;
    }

    public boolean isNextManeuverAtLeastOneSecondInFront() {
        return maneuverWithEstimationData.getCurveWithUnstableCourseAndSpeed()
                .getDurationFromManeuverEndToNextManeuverStart() != null
                && maneuverWithEstimationData.getCurveWithUnstableCourseAndSpeed()
                        .getDurationFromManeuverEndToNextManeuverStart().asSeconds() >= 1;
    }

    @Override
    public boolean isPreviousManeuverAtLeastOneSecondBehind() {
        return maneuverWithEstimationData.getCurveWithUnstableCourseAndSpeed()
                .getDurationFromPreviousManeuverEndToManeuverStart() != null
                && maneuverWithEstimationData.getCurveWithUnstableCourseAndSpeed()
                        .getDurationFromPreviousManeuverEndToManeuverStart().asSeconds() >= 1;
    }

    @Override
    public CompleteManeuverCurveWithEstimationData getCompleteManeuverCurveWithEstimationData() {
        return maneuverWithEstimationData;
    }

    @Override
    public ClusterDTO getJibingCount() {
        return new ClusterDTO(maneuverWithEstimationData.getJibingCount() + "");
    }

    @Override
    public ClusterDTO getTackingCount() {
        return new ClusterDTO(maneuverWithEstimationData.getTackingCount() + "");
    }

    @Override
    public Bearing getAbsTwaAtMaxTurningRate() {
        return getAbsTwaFromCourse(maneuverWithEstimationData.getMainCurve().getCourseAtMaxTurningRate());
    }

    @Override
    public Bearing getAbsTwaAtLowestSpeed() {
        return getAbsTwaFromCourse(maneuverWithEstimationData.getMainCurve().getLowestSpeed().getBearing());
    }

    @Override
    public Bearing getAbsTwaAtHighestSpeed() {
        return getAbsTwaFromCourse(maneuverWithEstimationData.getMainCurve().getHighestSpeed().getBearing());
    }

    private Bearing getAbsTwaFromCourse(Bearing course) {
        double twa = maneuverWithEstimationData.getWind().getFrom().getDifferenceTo(course).getDegrees();
        return new DegreeBearingImpl(twa);
    }

}

package com.sap.sailing.windestimation.data.transformer;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.ManeuverCategory;
import com.sap.sailing.windestimation.data.ManeuverForDataAnalysis;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;

public class ManeuverForDataAnalysisTransformer
        extends AbstractCompleteManeuverCurveWithEstimationDataTransformer<ManeuverForDataAnalysis> {

    @Override
    public List<ManeuverForDataAnalysis> transformElements(
            CompetitorTrackWithEstimationData<CompleteManeuverCurveWithEstimationData> competitorTrackWithElementsToTransform) {
        double speedScalingDivisor = getSpeedScalingDivisor(competitorTrackWithElementsToTransform.getElements());
        List<ManeuverForDataAnalysis> maneuversForClassification = new ArrayList<>();
        CompleteManeuverCurveWithEstimationData previousManeuver = null;
        CompleteManeuverCurveWithEstimationData maneuver = null;
        for (CompleteManeuverCurveWithEstimationData nextManeuver : competitorTrackWithElementsToTransform
                .getElements()) {
            if (maneuver != null) {
                ManeuverForDataAnalysis maneuverForClassification = getManeuverForDataAnalysis(maneuver,
                        previousManeuver, nextManeuver, speedScalingDivisor);
                if (maneuverForClassification != null) {
                    maneuversForClassification.add(maneuverForClassification);
                }
            }
            previousManeuver = maneuver;
            maneuver = nextManeuver;
        }
        if (maneuver != null) {
            ManeuverForDataAnalysis maneuverForClassification = getManeuverForDataAnalysis(maneuver, previousManeuver,
                    null, speedScalingDivisor);
            if (maneuverForClassification != null) {
                maneuversForClassification.add(maneuverForClassification);
            }
        }
        return maneuversForClassification;
    }

    private ManeuverForDataAnalysis getManeuverForDataAnalysis(CompleteManeuverCurveWithEstimationData maneuver,
            CompleteManeuverCurveWithEstimationData previousManeuver,
            CompleteManeuverCurveWithEstimationData nextManeuver, double speedScalingDivisor) {
        if (maneuver.getWind() == null) {
            return null;
        }
        ManeuverTypeForClassification maneuverType = getManeuverTypeForClassification(maneuver);
        double absoluteTotalCourseChangeInDegrees = Math
                .abs(maneuver.getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees());
        double absoluteTotalCourseChangeWithinMainCurveInDegrees = Math
                .abs(maneuver.getMainCurve().getDirectionChangeInDegrees());
        double speedInSpeedOutRatio = maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter()
                .getKnots() < 0.1 ? 0
                        : maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore().getKnots()
                                / maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getKnots();
        double oversteeringInDegrees = Math
                .abs(maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getBearing()
                        .getDifferenceTo(maneuver.getMainCurve().getSpeedWithBearingAfter().getBearing()).getDegrees());
        double speedLossRatio = maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore().getKnots() > 0
                ? maneuver.getCurveWithUnstableCourseAndSpeed().getLowestSpeed().getKnots()
                        / maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore().getKnots()
                : 0;
        double speedGainRatio = maneuver.getMainCurve().getHighestSpeed().getKnots() > 0
                ? maneuver.getMainCurve().getSpeedWithBearingBefore().getKnots()
                        / maneuver.getMainCurve().getHighestSpeed().getKnots()
                : 0;
        double lowestSpeedVsExitingSpeedRatio = maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter()
                .getKnots() > 0
                        ? maneuver.getCurveWithUnstableCourseAndSpeed().getLowestSpeed().getKnots()
                                / maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getKnots()
                        : 0;
        Double deviationFromOptimalTackAngleInDegrees = maneuver.getTargetTackAngleInDegrees() == null ? null
                : Math.abs(maneuver.getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees())
                        - maneuver.getTargetTackAngleInDegrees();
        Double deviationFromOptimalJibeAngleInDegrees = maneuver.getTargetJibeAngleInDegrees() == null ? null
                : Math.abs(maneuver.getCurveWithUnstableCourseAndSpeed().getDirectionChangeInDegrees())
                        - maneuver.getTargetJibeAngleInDegrees();
        Double highestAbsoluteDeviationOfBoatsCourseToBearingFromBoatToNextWaypointInDegrees;
        if (maneuver.getRelativeBearingToNextMarkBeforeManeuver() == null
                && maneuver.getRelativeBearingToNextMarkAfterManeuver() == null) {
            highestAbsoluteDeviationOfBoatsCourseToBearingFromBoatToNextWaypointInDegrees = null;
        } else if (maneuver.getRelativeBearingToNextMarkBeforeManeuver() == null) {
            highestAbsoluteDeviationOfBoatsCourseToBearingFromBoatToNextWaypointInDegrees = Math
                    .abs(maneuver.getRelativeBearingToNextMarkAfterManeuver().getDegrees());
        } else if (maneuver.getRelativeBearingToNextMarkAfterManeuver() == null) {
            highestAbsoluteDeviationOfBoatsCourseToBearingFromBoatToNextWaypointInDegrees = Math
                    .abs(maneuver.getRelativeBearingToNextMarkBeforeManeuver().getDegrees());
        } else {
            highestAbsoluteDeviationOfBoatsCourseToBearingFromBoatToNextWaypointInDegrees = Math
                    .abs(maneuver.getRelativeBearingToNextMarkBeforeManeuver().getDegrees()) > Math
                            .abs(maneuver.getRelativeBearingToNextMarkAfterManeuver().getDegrees())
                                    ? Math.abs(maneuver.getRelativeBearingToNextMarkBeforeManeuver().getDegrees())
                                    : Math.abs(maneuver.getRelativeBearingToNextMarkAfterManeuver().getDegrees());
        }
        double mainCurveDurationInSeconds = maneuver.getMainCurve().getDuration().asSeconds();
        double maneuverDurationInSeconds = maneuver.getCurveWithUnstableCourseAndSpeed().getDuration().asSeconds();
        double recoveryPhaseDurationInSeconds = maneuver.getMainCurve().getTimePointAfter()
                .until(maneuver.getCurveWithUnstableCourseAndSpeed().getTimePointAfter()).asSeconds();
        double timeLossInSeconds = maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore()
                .getMetersPerSecond() > 0
                        ? maneuver.getCurveWithUnstableCourseAndSpeed().getDistanceLost().getMeters()
                                / maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore()
                                        .getMetersPerSecond()
                        : 0;
        boolean clean = isManeuverClean(maneuver, previousManeuver, nextManeuver);
        ManeuverCategory maneuverCategory = getManeuverCategory(maneuver);
        double twaBeforeInDegrees = maneuver.getWind().getFrom()
                .getDifferenceTo(maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore().getBearing())
                .getDegrees();
        double twaAfterInDegrees = maneuver.getWind().getFrom()
                .getDifferenceTo(maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getBearing())
                .getDegrees();
        double twsInKnots = maneuver.getWind().getKnots();
        double speedBeforeInKnots = maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingBefore()
                .getKnots();
        double speedAfterInKnots = maneuver.getCurveWithUnstableCourseAndSpeed().getSpeedWithBearingAfter().getKnots();
        double twaAtMiddleCourseInDegrees = maneuver.getWind().getFrom()
                .getDifferenceTo(maneuver.getCurveWithUnstableCourseAndSpeed().getMiddleCourse()).getDegrees();
        double twaAtMiddleCourseMainCurveInDegrees = maneuver.getWind().getFrom()
                .getDifferenceTo(maneuver.getMainCurve().getMiddleCourse()).getDegrees();
        double twaAtLowestSpeedInDegrees = maneuver.getWind().getFrom()
                .getDifferenceTo(maneuver.getMainCurve().getLowestSpeed().getBearing()).getDegrees();
        double twaAtMaxTurningRateInDegrees = maneuver.getWind().getFrom()
                .getDifferenceTo(maneuver.getMainCurve().getCourseAtMaxTurningRate()).getDegrees();
        boolean starboardManeuver = maneuver.getMainCurve().getDirectionChangeInDegrees() > 0;
        ManeuverForDataAnalysis maneuverForClassification = new ManeuverForDataAnalysis(maneuverType,
                absoluteTotalCourseChangeInDegrees, absoluteTotalCourseChangeWithinMainCurveInDegrees,
                speedInSpeedOutRatio, oversteeringInDegrees, speedLossRatio, speedGainRatio,
                lowestSpeedVsExitingSpeedRatio, maneuver.getMainCurve().getMaxTurningRateInDegreesPerSecond(),
                deviationFromOptimalTackAngleInDegrees, deviationFromOptimalJibeAngleInDegrees,
                highestAbsoluteDeviationOfBoatsCourseToBearingFromBoatToNextWaypointInDegrees,
                mainCurveDurationInSeconds, maneuverDurationInSeconds, recoveryPhaseDurationInSeconds,
                timeLossInSeconds, clean, maneuverCategory, twaBeforeInDegrees, twaAfterInDegrees, twsInKnots,
                speedBeforeInKnots, speedAfterInKnots, twaAtMiddleCourseInDegrees, twaAtMiddleCourseMainCurveInDegrees,
                twaAtLowestSpeedInDegrees, twaAtMaxTurningRateInDegrees, starboardManeuver,
                speedBeforeInKnots / speedScalingDivisor, speedAfterInKnots / speedScalingDivisor);
        return maneuverForClassification;
    }

}

package com.sap.sailing.windestimation.data.transformer;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.windestimation.data.LabelledManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;

public class LabelledManeuverForEstimationTransformer extends ManeuverForEstimationTransformer {

    @Override
    protected LabelledManeuverForEstimation getManeuverForEstimation(CompleteManeuverCurveWithEstimationData maneuver,
            CompleteManeuverCurveWithEstimationData previousManeuver,
            CompleteManeuverCurveWithEstimationData nextManeuver, double speedScalingDivisor, BoatClass boatClass,
            String regattaName) {
        ManeuverForEstimation maneuverForEstimation = super.getManeuverForEstimation(maneuver, previousManeuver,
                nextManeuver, speedScalingDivisor, boatClass, regattaName);
        ManeuverTypeForClassification maneuverType = getManeuverTypeForClassification(maneuver);
        LabelledManeuverForEstimation labelledManeuverForEstimation = new LabelledManeuverForEstimation(
                maneuverForEstimation.getManeuverTimePoint(), maneuverForEstimation.getManeuverPosition(),
                maneuverForEstimation.getMiddleCourse(), maneuverForEstimation.getSpeedWithBearingBefore(),
                maneuverForEstimation.getSpeedWithBearingAfter(), maneuverForEstimation.getCourseChangeInDegrees(),
                maneuverForEstimation.getCourseChangeWithinMainCurveInDegrees(),
                maneuverForEstimation.getMaxTurningRateInDegreesPerSecond(),
                maneuverForEstimation.getDeviationFromOptimalTackAngleInDegrees(),
                maneuverForEstimation.getDeviationFromOptimalJibeAngleInDegrees(),
                maneuverForEstimation.getSpeedLossRatio(), maneuverForEstimation.getSpeedGainRatio(),
                maneuverForEstimation.getLowestSpeedVsExitingSpeedRatio(), maneuverForEstimation.isClean(),
                maneuverForEstimation.isCleanBefore(), maneuverForEstimation.isCleanAfter(),
                maneuverForEstimation.getManeuverCategory(), maneuverForEstimation.getScaledSpeedBefore(),
                maneuverForEstimation.getScaledSpeedAfter(), maneuverForEstimation.isMarkPassing(),
                maneuverForEstimation.getBoatClass(), maneuverForEstimation.isMarkPassingDataAvailable(), maneuverType,
                maneuver.getWind(), regattaName);
        return labelledManeuverForEstimation;
    }

    protected ManeuverTypeForClassification getManeuverTypeForClassification(
            CompleteManeuverCurveWithEstimationData maneuver) {
        ManeuverType maneuverType = maneuver.getManeuverTypeForCompleteManeuverCurve();
        switch (maneuverType) {
        case BEAR_AWAY:
            return ManeuverTypeForClassification.BEAR_AWAY;
        case HEAD_UP:
            return ManeuverTypeForClassification.HEAD_UP;
        case PENALTY_CIRCLE:
        case UNKNOWN:
            return null;
        case JIBE:
            return ManeuverTypeForClassification.JIBE;
        case TACK:
            return ManeuverTypeForClassification.TACK;
        }
        throw new IllegalStateException();
    }

}

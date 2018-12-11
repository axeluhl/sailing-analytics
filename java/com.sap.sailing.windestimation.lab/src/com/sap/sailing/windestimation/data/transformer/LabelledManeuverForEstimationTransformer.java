package com.sap.sailing.windestimation.data.transformer;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.maneuverdetection.CompleteManeuverCurveWithEstimationData;
import com.sap.sailing.windestimation.data.LabelledManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverCategory;
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
        ManeuverTypeForClassification maneuverType = getManeuverTypeForClassification(maneuver,
                maneuverForEstimation.getManeuverCategory());
        LabelledManeuverForEstimation labelledManeuverForEstimation = new LabelledManeuverForEstimation(
                maneuverForEstimation.getManeuverTimePoint(), maneuverForEstimation.getManeuverPosition(),
                maneuverForEstimation.getMiddleCourse(), maneuverForEstimation.getSpeedWithBearingBefore(),
                maneuverForEstimation.getSpeedWithBearingAfter(), maneuverForEstimation.getCourseAtLowestSpeed(),
                maneuverForEstimation.getAverageSpeedWithBearingBefore(),
                maneuverForEstimation.getAverageSpeedWithBearingAfter(),
                maneuverForEstimation.getCourseChangeInDegrees(),
                maneuverForEstimation.getCourseChangeWithinMainCurveInDegrees(),
                maneuverForEstimation.getMaxTurningRateInDegreesPerSecond(),
                maneuverForEstimation.getDeviationFromOptimalTackAngleInDegrees(),
                maneuverForEstimation.getDeviationFromOptimalJibeAngleInDegrees(),
                maneuverForEstimation.getSpeedLossRatio(), maneuverForEstimation.getSpeedGainRatio(),
                maneuverForEstimation.getLowestSpeedVsExitingSpeedRatio(), maneuverForEstimation.isClean(),
                maneuverForEstimation.isCleanBefore(), maneuverForEstimation.isCleanAfter(),
                maneuverForEstimation.getManeuverCategory(), maneuverForEstimation.getScaledSpeedBefore(),
                maneuverForEstimation.getScaledSpeedAfter(), maneuverForEstimation.getBoatClass(),
                maneuverForEstimation.isMarkPassing(), maneuverForEstimation.getRelativeBearingToNextMarkBefore(),
                maneuverForEstimation.getRelativeBearingToNextMarkAfter(), maneuverForEstimation.getRegattaName(),
                maneuverType, maneuver.getWind());
        return labelledManeuverForEstimation;
    }

    protected ManeuverTypeForClassification getManeuverTypeForClassification(
            CompleteManeuverCurveWithEstimationData maneuver, ManeuverCategory maneuverCategory) {
        switch (maneuverCategory) {
        case _180:
        case _360:
        case SMALL:
        case WIDE:
            return null;
        case MARK_PASSING:
        case REGULAR:
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
        }
        throw new IllegalStateException();
    }

}

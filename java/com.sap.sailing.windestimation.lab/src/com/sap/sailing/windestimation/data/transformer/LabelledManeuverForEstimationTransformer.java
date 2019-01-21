package com.sap.sailing.windestimation.data.transformer;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.windestimation.data.CompetitorTrackWithEstimationData;
import com.sap.sailing.windestimation.data.LabelledManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;

public class LabelledManeuverForEstimationTransformer implements
        CompetitorTrackTransformer<ConvertableToLabelledManeuverForEstimation, LabelledManeuverForEstimation> {

    private final ManeuverForEstimationTransformer internalTransformer = new ManeuverForEstimationTransformer();

    public LabelledManeuverForEstimation getManeuverForEstimation(ConvertableToLabelledManeuverForEstimation maneuver,
            ConvertableToLabelledManeuverForEstimation previousManeuver,
            ConvertableToLabelledManeuverForEstimation nextManeuver, double speedScalingDivisor, BoatClass boatClass,
            String regattaName) {
        ManeuverForEstimation maneuverForEstimation = internalTransformer.getManeuverForEstimation(maneuver,
                speedScalingDivisor, boatClass);
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
                maneuverForEstimation.getManeuverCategory(), maneuverForEstimation.getScaledSpeedBefore(),
                maneuverForEstimation.getScaledSpeedAfter(), maneuverForEstimation.isMarkPassing(),
                maneuverForEstimation.getBoatClass(), maneuverForEstimation.isMarkPassingDataAvailable(), maneuverType,
                maneuver.getWind(), regattaName);
        return labelledManeuverForEstimation;
    }

    protected ManeuverTypeForClassification getManeuverTypeForClassification(
            ConvertableToLabelledManeuverForEstimation maneuver) {
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

    public List<LabelledManeuverForEstimation> getManeuversForEstimation(
            List<ConvertableToLabelledManeuverForEstimation> convertableManeuvers, BoatClass boatClass,
            String regattaName) {
        double speedScalingDivisor = internalTransformer.getSpeedScalingDivisor(convertableManeuvers);
        List<LabelledManeuverForEstimation> maneuversForEstimation = new ArrayList<>();
        ConvertableToLabelledManeuverForEstimation previousManeuver = null;
        ConvertableToLabelledManeuverForEstimation maneuver = null;
        for (ConvertableToLabelledManeuverForEstimation nextManeuver : convertableManeuvers) {
            if (maneuver != null) {
                LabelledManeuverForEstimation maneuverForEstimation = getManeuverForEstimation(maneuver,
                        previousManeuver, nextManeuver, speedScalingDivisor, boatClass, regattaName);
                if (maneuverForEstimation != null) {
                    maneuversForEstimation.add(maneuverForEstimation);
                }
            }
            previousManeuver = maneuver;
            maneuver = nextManeuver;
        }
        if (maneuver != null) {
            LabelledManeuverForEstimation maneuverForEstimation = getManeuverForEstimation(maneuver, previousManeuver,
                    null, speedScalingDivisor, boatClass, regattaName);
            if (maneuverForEstimation != null) {
                maneuversForEstimation.add(maneuverForEstimation);
            }
        }
        return maneuversForEstimation;
    }

    @Override
    public List<LabelledManeuverForEstimation> transformElements(
            CompetitorTrackWithEstimationData<ConvertableToLabelledManeuverForEstimation> competitorTrackWithElementsToTransform) {
        return getManeuversForEstimation(competitorTrackWithElementsToTransform.getElements(),
                competitorTrackWithElementsToTransform.getBoatClass(),
                competitorTrackWithElementsToTransform.getRegattaName());
    }

}

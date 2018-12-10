package com.sap.sailing.windestimation.classifier.maneuver;

import com.sap.sailing.windestimation.classifier.ClassificationResultMapper;
import com.sap.sailing.windestimation.data.ManeuverCategory;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;

public class ManeuverClassificationResultMapper implements
        ClassificationResultMapper<ManeuverForEstimation, ManeuverModelMetadata, ManeuverWithProbabilisticTypeClassification> {

    private double[] mapManeuverTypesForInternalClassificationToManeuverTypesForClassification(
            double[] likelihoodPerManeuverType, ManeuverCategory maneuverCategory) {
        double[] newLikelihoods = new double[ManeuverTypeForClassification.values().length];
        switch (maneuverCategory) {
        case MARK_PASSING:
        case REGULAR:
            newLikelihoods[ManeuverTypeForClassification.TACK
                    .ordinal()] = likelihoodPerManeuverType[ManeuverTypeForInternalClassification.TACK.ordinal()];
            newLikelihoods[ManeuverTypeForClassification.JIBE
                    .ordinal()] = likelihoodPerManeuverType[ManeuverTypeForInternalClassification.JIBE.ordinal()];
            newLikelihoods[ManeuverTypeForClassification.BEAR_AWAY
                    .ordinal()] = likelihoodPerManeuverType[ManeuverTypeForInternalClassification.OTHER.ordinal()];
            newLikelihoods[ManeuverTypeForClassification.HEAD_UP
                    .ordinal()] = likelihoodPerManeuverType[ManeuverTypeForInternalClassification.OTHER.ordinal()];
            break;
        default:
            throw new IllegalArgumentException("Unsupported maneuver category");
        }
        return newLikelihoods;
    }

    @Override
    public ManeuverWithProbabilisticTypeClassification mapToClassificationResult(double[] likelihoods,
            ManeuverForEstimation maneuver, ManeuverModelMetadata modelMetadata) {
        likelihoods = modelMetadata.getLikelihoodsPerManeuverTypeOrdinal(likelihoods);
        likelihoods = mapManeuverTypesForInternalClassificationToManeuverTypesForClassification(likelihoods,
                maneuver.getManeuverCategory());
        ManeuverWithProbabilisticTypeClassification maneuverClassificationResult = new ManeuverWithProbabilisticTypeClassification(
                maneuver, likelihoods);
        return maneuverClassificationResult;
    }

}

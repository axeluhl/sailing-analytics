package com.sap.sailing.windestimation.model.classifier.maneuver;

import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.model.classifier.ClassificationResultMapper;

public class ManeuverClassificationResultMapper implements
        ClassificationResultMapper<ManeuverForEstimation, ManeuverClassifierModelMetadata, ManeuverWithProbabilisticTypeClassification> {

    @Override
    public ManeuverWithProbabilisticTypeClassification mapToClassificationResult(double[] likelihoods,
            ManeuverForEstimation maneuver, ManeuverClassifierModelMetadata modelMetadata) {
        likelihoods = modelMetadata.getLikelihoodsPerManeuverTypeOrdinal(likelihoods);
        ManeuverWithProbabilisticTypeClassification maneuverClassificationResult = new ManeuverWithProbabilisticTypeClassification(
                maneuver, likelihoods);
        return maneuverClassificationResult;
    }

}

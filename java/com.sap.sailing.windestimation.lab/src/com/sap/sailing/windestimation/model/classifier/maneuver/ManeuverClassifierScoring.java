package com.sap.sailing.windestimation.model.classifier.maneuver;

import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.model.classifier.ClassifierScoring;
import com.sap.sailing.windestimation.model.classifier.TrainableClassificationModel;

public class ManeuverClassifierScoring
        extends ClassifierScoring<ManeuverForEstimation, ManeuverClassifierModelMetadata> {

    public ManeuverClassifierScoring(
            TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelMetadata> trainedClassifierModel) {
        super(trainedClassifierModel, i -> {
            ManeuverClassifierModelMetadata modelMetadata = trainedClassifierModel.getContextSpecificModelMetadata();
            ManeuverTypeForClassification maneuverType = trainedClassifierModel.getContextSpecificModelMetadata()
                    .getManeuverTypeByMappingIndex(i);
            if (modelMetadata.getOtherTypes() <= 1) {
                return maneuverType.toString();
            }
            int otherTypesBeginFromOrdinal = ManeuverTypeForClassification.values().length
                    - modelMetadata.getOtherTypes();
            if (maneuverType.ordinal() < otherTypesBeginFromOrdinal) {
                return maneuverType.toString();
            }
            return "Other";
        });
    }

}

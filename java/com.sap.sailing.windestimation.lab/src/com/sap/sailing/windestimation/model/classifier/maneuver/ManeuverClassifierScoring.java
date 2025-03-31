package com.sap.sailing.windestimation.model.classifier.maneuver;

import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.data.ManeuverTypeForClassification;
import com.sap.sailing.windestimation.model.classifier.ClassifierScoring;
import com.sap.sailing.windestimation.model.classifier.TrainableClassificationModel;

public class ManeuverClassifierScoring
        extends ClassifierScoring<ManeuverForEstimation, ManeuverClassifierModelContext> {

    public ManeuverClassifierScoring(
            TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelContext> trainedClassifierModel) {
        super(trainedClassifierModel, i -> {
            ManeuverClassifierModelContext modelContext = trainedClassifierModel.getModelContext();
            ManeuverTypeForClassification maneuverType = trainedClassifierModel.getModelContext()
                    .getManeuverTypeByMappingIndex(i);
            if (modelContext.getOtherTypes() <= 1) {
                return maneuverType.toString();
            }
            int otherTypesBeginFromOrdinal = ManeuverTypeForClassification.values().length
                    - modelContext.getOtherTypes();
            if (maneuverType.ordinal() < otherTypesBeginFromOrdinal) {
                return maneuverType.toString();
            }
            return "Other";
        });
    }

}

package com.sap.sailing.windestimation.classifier.maneuver;

import com.sap.sailing.windestimation.classifier.ClassifierScoring;
import com.sap.sailing.windestimation.classifier.TrainableClassificationModel;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;

public class ManeuverClassifierScoring extends ClassifierScoring<ManeuverForEstimation, ManeuverModelMetadata> {

    public ManeuverClassifierScoring(
            TrainableClassificationModel<ManeuverForEstimation, ManeuverModelMetadata> trainedClassifierModel) {
        super(trainedClassifierModel, i -> trainedClassifierModel.getModelMetadata().getContextSpecificModelMetadata()
                .getManeuverTypeByMappingIndex(i).toString());
    }

}

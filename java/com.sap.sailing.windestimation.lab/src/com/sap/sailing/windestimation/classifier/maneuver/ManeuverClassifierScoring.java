package com.sap.sailing.windestimation.classifier.maneuver;

import com.sap.sailing.windestimation.classifier.ClassifierScoring;
import com.sap.sailing.windestimation.classifier.TrainableClassificationModel;
import com.sap.sailing.windestimation.data.ManeuverForEstimation;

public class ManeuverClassifierScoring extends ClassifierScoring<ManeuverForEstimation, ManeuverClassifierModelMetadata> {

    public ManeuverClassifierScoring(
            TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelMetadata> trainedClassifierModel) {
        super(trainedClassifierModel, i -> trainedClassifierModel.getContextSpecificModelMetadata()
                .getManeuverTypeByMappingIndex(i).toString());
    }

}

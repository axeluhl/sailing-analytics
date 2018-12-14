package com.sap.sailing.windestimation.model.classifier.maneuver;

import com.sap.sailing.windestimation.data.ManeuverForEstimation;
import com.sap.sailing.windestimation.model.classifier.ClassifierScoring;
import com.sap.sailing.windestimation.model.classifier.TrainableClassificationModel;
import com.sap.sailing.windestimation.model.classifier.maneuver.ManeuverClassifierModelMetadata;

public class ManeuverClassifierScoring extends ClassifierScoring<ManeuverForEstimation, ManeuverClassifierModelMetadata> {

    public ManeuverClassifierScoring(
            TrainableClassificationModel<ManeuverForEstimation, ManeuverClassifierModelMetadata> trainedClassifierModel) {
        super(trainedClassifierModel, i -> trainedClassifierModel.getContextSpecificModelMetadata()
                .getManeuverTypeByMappingIndex(i).toString());
    }

}

package com.sap.sailing.windestimation.classifier.smile;

import com.sap.sailing.windestimation.classifier.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.classifier.PreprocessingConfig.PreprocessingConfigBuilder;

import smile.classification.RandomForest;

public class RandomForestManeuverClassifier<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends AbstractSmileManeuverClassificationModel<InstanceType, T> {

    private static final long serialVersionUID = -3364152319152090775L;

    public RandomForestManeuverClassifier(T contextSpecificModelMetadata) {
        super(new PreprocessingConfigBuilder().build(), contextSpecificModelMetadata);
    }

    @Override
    protected RandomForest trainInternalModel(double[][] x, int[] y) {
        return new RandomForest(x, y, 50,
                getModelMetadata().getContextSpecificModelMetadata().getNumberOfInputFeatures());
    }

}

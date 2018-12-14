package com.sap.sailing.windestimation.model.classifier.smile;

import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.model.classifier.PreprocessingConfig.PreprocessingConfigBuilder;

import smile.classification.RandomForest;

public class RandomForestClassifier<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends AbstractSmileClassificationModel<InstanceType, T> {

    private static final long serialVersionUID = -3364152319152090775L;

    public RandomForestClassifier(T contextSpecificModelMetadata) {
        super(new PreprocessingConfigBuilder().build(), contextSpecificModelMetadata);
    }

    @Override
    protected RandomForest trainInternalModel(double[][] x, int[] y) {
        return new RandomForest(x, y, 50, getContextSpecificModelMetadata().getNumberOfInputFeatures());
    }

}

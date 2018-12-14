package com.sap.sailing.windestimation.classifier.smile;

import com.sap.sailing.windestimation.classifier.PreprocessingConfig.PreprocessingConfigBuilder;
import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;

import smile.classification.GradientTreeBoost;

public class GradientBoostingClassifier<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends AbstractSmileClassificationModel<InstanceType, T> {

    private static final long serialVersionUID = -3364152319152090775L;

    public GradientBoostingClassifier(T contextSpecificModelMetadata) {
        super(new PreprocessingConfigBuilder().scaling().build(), contextSpecificModelMetadata);
    }

    @Override
    protected GradientTreeBoost trainInternalModel(double[][] x, int[] y) {
        return new GradientTreeBoost(x, y, 50);
    }

}

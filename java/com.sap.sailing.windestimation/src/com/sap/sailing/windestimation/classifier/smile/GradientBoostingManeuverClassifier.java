package com.sap.sailing.windestimation.classifier.smile;

import com.sap.sailing.windestimation.classifier.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.classifier.PreprocessingConfig.PreprocessingConfigBuilder;

import smile.classification.GradientTreeBoost;

public class GradientBoostingManeuverClassifier<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends AbstractSmileManeuverClassificationModel<InstanceType, T> {

    private static final long serialVersionUID = -3364152319152090775L;

    public GradientBoostingManeuverClassifier(T contextSpecificModelMetadata) {
        super(new PreprocessingConfigBuilder().scaling().build(), contextSpecificModelMetadata);
    }

    @Override
    protected GradientTreeBoost trainInternalModel(double[][] x, int[] y) {
        return new GradientTreeBoost(x, y, 50);
    }

}

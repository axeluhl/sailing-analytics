package com.sap.sailing.windestimation.classifier.smile;

import com.sap.sailing.windestimation.classifier.ContextSpecificModelMetadata;
import com.sap.sailing.windestimation.classifier.PreprocessingConfig.PreprocessingConfigBuilder;

import smile.classification.LDA;

public class LDAManeuverClassifier<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>>
        extends AbstractSmileManeuverClassificationModel<InstanceType, T> {

    private static final long serialVersionUID = -3364152319152090775L;

    public LDAManeuverClassifier(T contextSpecificModelMetadata) {
        super(new PreprocessingConfigBuilder().scaling().build(), contextSpecificModelMetadata);
    }

    @Override
    protected LDA trainInternalModel(double[][] x, int[] y) {
        return new LDA(x, y);
    }

}

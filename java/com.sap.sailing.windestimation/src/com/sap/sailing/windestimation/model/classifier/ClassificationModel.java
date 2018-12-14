package com.sap.sailing.windestimation.model.classifier;

import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;

public interface ClassificationModel<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>> {

    double[] classifyWithProbabilities(double[] x);

    default double[] classifyWithProbabilities(InstanceType instance) {
        double[] x = getContextSpecificModelMetadata().getX(instance);
        return classifyWithProbabilities(x);
    }

    PreprocessingConfig getPreprocessingConfig();

    T getContextSpecificModelMetadata();

}

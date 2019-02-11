package com.sap.sailing.windestimation.model.classifier;

import com.sap.sailing.windestimation.model.ModelContext;

public interface ClassificationModel<InstanceType, T extends ModelContext<InstanceType>> {

    double[] classifyWithProbabilities(double[] x);

    default double[] classifyWithProbabilities(InstanceType instance) {
        double[] x = getModelContext().getX(instance);
        return classifyWithProbabilities(x);
    }

    PreprocessingConfig getPreprocessingConfig();

    T getModelContext();

}

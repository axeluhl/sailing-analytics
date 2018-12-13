package com.sap.sailing.windestimation.classifier;

public interface ClassificationModel<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>> {

    double[] classifyWithProbabilities(double[] x);

    default double[] classifyWithProbabilities(InstanceType instance) {
        double[] x = getModelMetadata().getContextSpecificModelMetadata().getX(instance);
        return classifyWithProbabilities(x);
    }

    ModelMetadata<InstanceType, T> getModelMetadata();

}

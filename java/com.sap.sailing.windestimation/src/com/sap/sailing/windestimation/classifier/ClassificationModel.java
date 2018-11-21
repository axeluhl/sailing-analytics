package com.sap.sailing.windestimation.classifier;

public interface ClassificationModel<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>> {

    double[] classifyWithProbabilities(double[] x);

    ModelMetadata<InstanceType, T> getModelMetadata();

    double getTestScore();

    double getTrainScore();

    boolean hasSupportForProvidedFeatures();

    boolean isModelReady();

    int getNumberOfTrainingInstances();

}

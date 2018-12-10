package com.sap.sailing.windestimation.classifier;

public interface ClassificationResultMapper<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>, ResultType> {

    ResultType mapToClassificationResult(double[] likelihoods, InstanceType instance, T contextSpecificModelMetadata);

}

package com.sap.sailing.windestimation.classifier;

import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;

public interface ClassificationResultMapper<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>, ResultType> {

    ResultType mapToClassificationResult(double[] likelihoods, InstanceType instance, T contextSpecificModelMetadata);

}

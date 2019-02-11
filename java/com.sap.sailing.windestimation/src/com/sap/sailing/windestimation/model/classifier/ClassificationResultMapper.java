package com.sap.sailing.windestimation.model.classifier;

import com.sap.sailing.windestimation.model.ModelContext;

public interface ClassificationResultMapper<InstanceType, T extends ModelContext<InstanceType>, ResultType> {

    ResultType mapToClassificationResult(double[] likelihoods, InstanceType instance, T modelContext);

}

package com.sap.sailing.windestimation.regression;

import com.sap.sailing.windestimation.model.ContextSpecificModelMetadata;

public interface RegressionModel<InstanceType, T extends ContextSpecificModelMetadata<InstanceType>> {
    default double getValue(InstanceType instance) {
        double[] x = getContextSpecificModelMetadata().getX(instance);
        return getValue(x);
    }

    double getValue(double[] x);

    T getContextSpecificModelMetadata();

}

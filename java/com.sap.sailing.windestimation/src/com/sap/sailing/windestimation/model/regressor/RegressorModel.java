package com.sap.sailing.windestimation.model.regressor;

import com.sap.sailing.windestimation.model.ModelContext;

public interface RegressorModel<InstanceType, T extends ModelContext<InstanceType>> {
    default double getValue(InstanceType instance) {
        double[] x = getModelContext().getX(instance);
        return getValue(x);
    }

    double getValue(double[] x);

    T getModelContext();

}

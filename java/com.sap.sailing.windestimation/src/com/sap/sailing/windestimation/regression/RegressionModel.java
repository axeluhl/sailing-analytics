package com.sap.sailing.windestimation.regression;

public interface RegressionModel<InstanceType> {
    double getY(InstanceType instanceType);

    double getY(double[] x);
}

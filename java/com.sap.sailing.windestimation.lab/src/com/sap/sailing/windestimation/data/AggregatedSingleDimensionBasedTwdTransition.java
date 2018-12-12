package com.sap.sailing.windestimation.data;

public class AggregatedSingleDimensionBasedTwdTransition {

    private final double dimensionValue;
    private final double mean;
    private final double std;

    public AggregatedSingleDimensionBasedTwdTransition(double dimensionValue, double mean, double std) {
        this.dimensionValue = dimensionValue;
        this.mean = mean;
        this.std = std;
    }

    public double getDimensionValue() {
        return dimensionValue;
    }

    public double getMean() {
        return mean;
    }

    public double getStd() {
        return std;
    }

}

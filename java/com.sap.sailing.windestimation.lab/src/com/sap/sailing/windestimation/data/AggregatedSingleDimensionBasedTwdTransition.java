package com.sap.sailing.windestimation.data;

public class AggregatedSingleDimensionBasedTwdTransition {

    private final double dimensionValue;
    private final double mean;
    private final double std;
    private final double median;
    private final long numberOfValues;

    public AggregatedSingleDimensionBasedTwdTransition(double dimensionValue, double mean, double std, double median,
            long numberOfValues) {
        this.dimensionValue = dimensionValue;
        this.mean = mean;
        this.std = std;
        this.median = median;
        this.numberOfValues = numberOfValues;
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

    public double getMedian() {
        return median;
    }

    public long getNumberOfValues() {
        return numberOfValues;
    }

}

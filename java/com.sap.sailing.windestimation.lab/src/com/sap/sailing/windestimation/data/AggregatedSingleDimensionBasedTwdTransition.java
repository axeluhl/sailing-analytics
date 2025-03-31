package com.sap.sailing.windestimation.data;

public class AggregatedSingleDimensionBasedTwdTransition {

    private final double dimensionValue;
    private final double mean;
    private final double std;
    private final double zeroMeanStd;
    private final double median;
    private final long numberOfValues;
    private double q1;
    private double q3;
    private double p1;
    private double p99;

    public AggregatedSingleDimensionBasedTwdTransition(double dimensionValue, double mean, double std, double zeroMeanStd, double median,
            long numberOfValues, double q1, double q3, double p1, double p99) {
        this.dimensionValue = dimensionValue;
        this.mean = mean;
        this.std = std;
        this.zeroMeanStd = zeroMeanStd;
        this.median = median;
        this.numberOfValues = numberOfValues;
        this.q1 = q1;
        this.q3 = q3;
        this.p1 = p1;
        this.p99 = p99;
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
    
    public double getZeroMeanStd() {
        return zeroMeanStd;
    }

    public double getMedian() {
        return median;
    }

    public long getNumberOfValues() {
        return numberOfValues;
    }

    public double getQ1() {
        return q1;
    }

    public double getQ3() {
        return q3;
    }

    public double getP1() {
        return p1;
    }

    public double getP99() {
        return p99;
    }

}

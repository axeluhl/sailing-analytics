package com.sap.sailing.windestimation.data;

public class SingleDimensionBasedTwdTransition {

    private final double dimensionValue;
    private final double twdChangeInDegrees;

    public SingleDimensionBasedTwdTransition(double dimensionValue, double twdChangeInDegrees) {
        this.dimensionValue = dimensionValue;
        this.twdChangeInDegrees = twdChangeInDegrees;
    }

    public double getDimensionValue() {
        return dimensionValue;
    }

    public double getTwdChangeInDegrees() {
        return twdChangeInDegrees;
    }

}

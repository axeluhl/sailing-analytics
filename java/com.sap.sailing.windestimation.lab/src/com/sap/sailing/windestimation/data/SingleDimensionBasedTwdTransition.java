package com.sap.sailing.windestimation.data;

public class SingleDimensionBasedTwdTransition {

    private final double dimensionValue;
    private final double absTwdChangeInDegrees;

    public SingleDimensionBasedTwdTransition(double dimensionValue, double absTwdChangeInDegrees) {
        this.dimensionValue = dimensionValue;
        this.absTwdChangeInDegrees = absTwdChangeInDegrees;
    }

    public double getDimensionValue() {
        return dimensionValue;
    }

    public double getAbsTwdChangeInDegrees() {
        return absTwdChangeInDegrees;
    }

}

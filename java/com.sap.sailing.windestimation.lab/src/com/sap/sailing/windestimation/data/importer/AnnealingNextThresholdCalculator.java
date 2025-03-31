package com.sap.sailing.windestimation.data.importer;

import smile.math.Math;

public class AnnealingNextThresholdCalculator implements NextThresholdCalculator {

    private final double initialThresholdValue;
    private final double annealingFactor;

    public AnnealingNextThresholdCalculator(double initialThresholdValue, double annealingFactor) {
        this.initialThresholdValue = initialThresholdValue;
        this.annealingFactor = annealingFactor;
    }

    @Override
    public double getInitialThresholdValue() {
        return initialThresholdValue;
    }

    @Override
    public double getNextThresholdValue(double previousThresholdValue) {
        return Math.ceil(previousThresholdValue * annealingFactor);
    }

}

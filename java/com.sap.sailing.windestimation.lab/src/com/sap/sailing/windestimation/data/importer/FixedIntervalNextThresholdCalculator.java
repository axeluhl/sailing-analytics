package com.sap.sailing.windestimation.data.importer;

public class FixedIntervalNextThresholdCalculator implements NextThresholdCalculator {

    private final double fixedInterval;

    public FixedIntervalNextThresholdCalculator(double fixedInterval) {
        this.fixedInterval = fixedInterval;
    }

    @Override
    public double getInitialThresholdValue() {
        return fixedInterval;
    }

    @Override
    public double getNextThresholdValue(double previousThresholdValue) {
        return previousThresholdValue + fixedInterval;
    }

}

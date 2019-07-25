package com.sap.sailing.windestimation.data.importer;

public interface NextThresholdCalculator {

    double getInitialThresholdValue();

    double getNextThresholdValue(double previousThresholdValue);

    default double getNextThresholdValue(double previousValue, double previousThresholdValue) {
        double nextThresholdValue = previousThresholdValue;
        while (nextThresholdValue <= previousValue) {
            nextThresholdValue = getNextThresholdValue(nextThresholdValue);
        }
        return nextThresholdValue;
    }

}

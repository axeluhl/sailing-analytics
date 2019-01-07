package com.sap.sailing.windestimation.data.importer;

public interface NextThresholdCalculator {

    double getInitialThresholdValue();

    double getNextThresholdValue(double previousThresholdValue);

}

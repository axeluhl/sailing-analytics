package com.sap.sailing.gwt.ui.client.shared.charts;

public class ChartSettings {
    private final long stepSize;
    
    public ChartSettings(long stepSize) {
        this.stepSize = stepSize;
    }

    /**
     * Copy-constructor
     */
    public ChartSettings(ChartSettings superResult) {
        this(superResult.getStepSize());
    }

    public long getStepSize() {
        return stepSize;
    }
}

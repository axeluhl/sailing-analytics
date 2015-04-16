package com.sap.sailing.gwt.ui.client.shared.charts;

import com.sap.sse.common.settings.Settings;

public class ChartSettings implements Settings {
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

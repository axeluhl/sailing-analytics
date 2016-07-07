package com.sap.sailing.gwt.ui.client.shared.charts;

import com.sap.sse.common.settings.AbstractSettings;

public class ChartSettings extends AbstractSettings {
    private final long stepSizeInMillis;
    
    public ChartSettings(long stepSizeInMillis) {
        this.stepSizeInMillis = stepSizeInMillis;
    }

    /**
     * Copy-constructor
     */
    public ChartSettings(ChartSettings superResult) {
        this(superResult.getStepSizeInMillis());
    }

    public long getStepSizeInMillis() {
        return stepSizeInMillis;
    }
}

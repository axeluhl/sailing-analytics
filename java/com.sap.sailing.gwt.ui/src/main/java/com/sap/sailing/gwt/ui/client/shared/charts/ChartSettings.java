package com.sap.sailing.gwt.ui.client.shared.charts;

import com.sap.sse.common.settings.generic.AbstractGenericSerializableSettings;
import com.sap.sse.common.settings.generic.LongSetting;
import com.sap.sse.security.ui.client.SecurityChildSettingsContext;

public class ChartSettings extends AbstractGenericSerializableSettings<SecurityChildSettingsContext> {
    
    private static final long serialVersionUID = 7041836078658713937L;
    
    private LongSetting stepSizeInMillis;
    
    @Override
    protected void addChildSettings(SecurityChildSettingsContext context) {
        stepSizeInMillis = new LongSetting("stepSizeInMillis", this, AbstractCompetitorRaceChart.DEFAULT_STEPSIZE);
    }
    
    public ChartSettings() {
        super(null);
    }
    
    public ChartSettings(long stepSizeInMillis) {
        this();
        this.stepSizeInMillis.setValue(stepSizeInMillis);
    }

    public long getStepSizeInMillis() {
        return stepSizeInMillis.getValue();
    }
}

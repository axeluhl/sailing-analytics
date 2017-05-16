package com.sap.sailing.gwt.ui.client.shared.charts;

import com.sap.sailing.gwt.ui.client.StringMessages;

public class ChartSettingsComponent extends AbstractChartSettingsComponent<ChartSettings> {
    
    public ChartSettingsComponent(ChartSettings settings, StringMessages stringMessages) {
        super(settings, stringMessages);
    }
    
    @Override
    public ChartSettings getResult() {
        return getAbstractResult();
    }
}

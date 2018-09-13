package com.sap.sailing.gwt.ui.client.shared.charts;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class WindChartLifecycle implements ComponentLifecycle<WindChartSettings> {
    private final StringMessages stringMessages;
    
    public static final String ID = "wc";

    public WindChartLifecycle(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
    }

    @Override
    public WindChartSettingsDialogComponent getSettingsDialogComponent(WindChartSettings settings) {
        return new WindChartSettingsDialogComponent(settings, stringMessages);
    }

    @Override
    public WindChartSettings createDefaultSettings() {
        return new WindChartSettings();
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.windChart();
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public String getComponentId() {
        return ID;
    }
}

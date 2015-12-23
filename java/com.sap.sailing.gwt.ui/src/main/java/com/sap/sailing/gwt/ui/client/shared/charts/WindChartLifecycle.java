package com.sap.sailing.gwt.ui.client.shared.charts;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class WindChartLifecycle implements ComponentLifecycle<WindChart, WindChartSettings, WindChartSettingsDialogComponent> {
    private final StringMessages stringMessages;
    private WindChart component;
    
    public WindChartLifecycle(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        this.component = null;
    }

    @Override
    public WindChartSettingsDialogComponent getSettingsDialogComponent(WindChartSettings settings) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public WindChart getComponent() {
        return component;
    }

    @Override
    public WindChartSettings createDefaultSettings() {
        return new WindChartSettings();
    }

    @Override
    public WindChartSettings cloneSettings(WindChartSettings settings) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.wind();
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

}

package com.sap.sailing.gwt.ui.client.shared.charts;

import java.io.Serializable;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class WindChartLifecycle implements ComponentLifecycle<WindChartSettings, WindChartSettingsDialogComponent> {
    private final StringMessages stringMessages;
    
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
    public WindChartSettings cloneSettings(WindChartSettings settings) {
        return new WindChartSettings(settings.isShowWindSpeedSeries(), settings.getWindSpeedSourcesToDisplay(),
                settings.isShowWindDirectionsSeries(), settings.getWindDirectionSourcesToDisplay(),
                settings.getResolutionInMilliseconds());
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.wind();
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public Serializable getComponentId() {
        return getLocalizedShortName();
    }
}

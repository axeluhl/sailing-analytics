package com.sap.sailing.gwt.ui.client.shared.charts;

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
    public String getLocalizedShortName() {
        return stringMessages.wind();
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public String getComponentId() {
        return "WindChart";
    }

    @Override
    public WindChartSettings extractGlobalSettings(WindChartSettings settings) {
        WindChartSettings def = createDefaultSettings();
        return new WindChartSettings(settings.isShowWindSpeedSeries(), def.getWindSpeedSourcesToDisplay(),
                settings.isShowWindDirectionsSeries(), def.getWindDirectionSourcesToDisplay(),
                settings.getResolutionInMilliseconds());
    }

    @Override
    public WindChartSettings extractContextSettings(WindChartSettings settings) {
        WindChartSettings def = createDefaultSettings();
        return new WindChartSettings(def.isShowWindSpeedSeries(), settings.getWindSpeedSourcesToDisplay(),
                def.isShowWindDirectionsSeries(), settings.getWindDirectionSourcesToDisplay(),
                def.getResolutionInMilliseconds());
    }
}

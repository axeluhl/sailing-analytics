package com.sap.sailing.gwt.autoplay.client.place.start;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.WindChartSettings;
import com.sap.sailing.gwt.ui.client.shared.charts.WindChartSettingsDialogComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class ProxyWindChartComponent implements Component<WindChartSettings> {
    private final StringMessages stringMessages;
    private WindChartSettings settings;
    
    public ProxyWindChartComponent(WindChartSettings settings, StringMessages stringMessages) {
        this.settings = settings;
        this.stringMessages = stringMessages;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<WindChartSettings> getSettingsDialogComponent() {
        return new WindChartSettingsDialogComponent(settings, stringMessages);
    }

    @Override
    public void updateSettings(WindChartSettings newSettings) {
        this.settings = newSettings;
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.settingsForComponent(stringMessages.windChart());
    }

    @Override
    public Widget getEntryWidget() {
        throw new UnsupportedOperationException(
                "Internal error. This settings dialog does not actually belong to a wind chart");
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public void setVisible(boolean visibility) {
        // no-op
    }

    @Override
    public String getDependentCssClassName() {
        return "windChartSettingsDialog";
    }
}
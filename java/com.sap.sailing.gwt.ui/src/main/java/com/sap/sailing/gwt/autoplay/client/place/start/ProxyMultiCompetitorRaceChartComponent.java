package com.sap.sailing.gwt.autoplay.client.place.start;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorRaceChartSettings;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorRaceChartSettingsComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class ProxyMultiCompetitorRaceChartComponent implements Component<MultiCompetitorRaceChartSettings> {
    private final StringMessages stringMessages;
    private MultiCompetitorRaceChartSettings settings;
    
    public ProxyMultiCompetitorRaceChartComponent(MultiCompetitorRaceChartSettings settings, StringMessages stringMessages) {
        this.settings = settings;
        this.stringMessages = stringMessages;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<MultiCompetitorRaceChartSettings> getSettingsDialogComponent() {
        return new MultiCompetitorRaceChartSettingsComponent(settings, stringMessages, true);
    }

    @Override
    public void updateSettings(MultiCompetitorRaceChartSettings newSettings) {
        this.settings = newSettings;
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.competitorCharts();
    }

    @Override
    public Widget getEntryWidget() {
        throw new UnsupportedOperationException(
                "Internal error. This settings dialog does not actually belong to a real chart");
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
        return "multiCompetitorRaceChart";
    }

    @Override
    public MultiCompetitorRaceChartSettings getSettings() {
        return settings;
    }
}

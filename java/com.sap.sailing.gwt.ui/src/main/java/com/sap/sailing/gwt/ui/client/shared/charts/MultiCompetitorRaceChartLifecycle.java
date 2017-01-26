package com.sap.sailing.gwt.ui.client.shared.charts;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class MultiCompetitorRaceChartLifecycle implements ComponentLifecycle<MultiCompetitorRaceChartSettings, MultiCompetitorRaceChartSettingsComponent> {
    private final StringMessages stringMessages;
    private final boolean hasOverallLeaderboard;
    
    public MultiCompetitorRaceChartLifecycle(StringMessages stringMessages, boolean hasOverallLeaderboard) {
        this.stringMessages = stringMessages;
        this.hasOverallLeaderboard = hasOverallLeaderboard;
    }

    @Override
    public MultiCompetitorRaceChartSettingsComponent getSettingsDialogComponent(MultiCompetitorRaceChartSettings settings) {
        return new MultiCompetitorRaceChartSettingsComponent(settings, stringMessages, hasOverallLeaderboard);
    }

    @Override
    public MultiCompetitorRaceChartSettings createDefaultSettings() {
        return new MultiCompetitorRaceChartSettings();
    }

    @Override
    public MultiCompetitorRaceChartSettings cloneSettings(MultiCompetitorRaceChartSettings settings) {
        return new MultiCompetitorRaceChartSettings(new ChartSettings(settings.getStepSizeInMillis()), settings.getFirstDetailType(), settings.getSecondDetailType());
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.competitorCharts();
    }

    @Override
    public String getComponentId() {
        return "MultiCompetitorRaceChart";
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public MultiCompetitorRaceChartSettings extractGlobalSettings(MultiCompetitorRaceChartSettings settings) {
        return settings;
    }

    @Override
    public MultiCompetitorRaceChartSettings extractContextSettings(MultiCompetitorRaceChartSettings settings) {
        return createDefaultSettings();
    }
}

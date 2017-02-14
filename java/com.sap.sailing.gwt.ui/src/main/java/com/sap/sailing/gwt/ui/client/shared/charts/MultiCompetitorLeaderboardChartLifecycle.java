package com.sap.sailing.gwt.ui.client.shared.charts;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class MultiCompetitorLeaderboardChartLifecycle implements
        ComponentLifecycle<MultiCompetitorLeaderboardChartSettings, MultiCompetitorLeaderboardChartSettingsDialogComponent> {
    
    public static final String ID = "mclcl";
    private final boolean isOverall;

    public MultiCompetitorLeaderboardChartLifecycle(boolean isOverall) {
        this.isOverall = isOverall;
    }

    @Override
    public MultiCompetitorLeaderboardChartSettingsDialogComponent getSettingsDialogComponent(
            MultiCompetitorLeaderboardChartSettings settings) {
        return new MultiCompetitorLeaderboardChartSettingsDialogComponent(settings, isOverall);
    }

    @Override
    public MultiCompetitorLeaderboardChartSettings createDefaultSettings() {
        return new MultiCompetitorLeaderboardChartSettings(null);
    }

    @Override
    public String getLocalizedShortName() {
        return StringMessages.INSTANCE.competitorCharts();
    }

    @Override
    public String getComponentId() {
        return ID;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public MultiCompetitorLeaderboardChartSettings extractGlobalSettings(
            MultiCompetitorLeaderboardChartSettings settings) {
        return settings;
    }

    @Override
    public MultiCompetitorLeaderboardChartSettings extractContextSettings(
            MultiCompetitorLeaderboardChartSettings settings) {
        return createDefaultSettings();
    }
}

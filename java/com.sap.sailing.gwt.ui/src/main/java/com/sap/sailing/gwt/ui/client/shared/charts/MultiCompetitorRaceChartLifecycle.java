package com.sap.sailing.gwt.ui.client.shared.charts;

import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.shared.components.ComponentLifecycle;

public class MultiCompetitorRaceChartLifecycle implements ComponentLifecycle<MultiCompetitorRaceChartSettings> {
    private final StringMessages stringMessages;
    private final boolean hasOverallLeaderboard;
    
    public static final String ID = "cc";
    
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
    public String getLocalizedShortName() {
        return stringMessages.competitorCharts();
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
    public MultiCompetitorRaceChartSettings extractGlobalSettings(MultiCompetitorRaceChartSettings settings) {
        return settings;
    }

    @Override
    public MultiCompetitorRaceChartSettings extractContextSpecificSettings(MultiCompetitorRaceChartSettings settings) {
        return settings;
    }
}

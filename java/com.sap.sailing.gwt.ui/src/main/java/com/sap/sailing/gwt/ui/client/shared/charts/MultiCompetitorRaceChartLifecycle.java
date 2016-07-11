package com.sap.sailing.gwt.ui.client.shared.charts;

import java.io.Serializable;

import com.sap.sailing.domain.common.DetailType;
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
        ChartSettings chartSettings = new ChartSettings(AbstractCompetitorRaceChart.DEFAULT_STEPSIZE);
        DetailType defaultDetailType = DetailType.WINDWARD_DISTANCE_TO_COMPETITOR_FARTHEST_AHEAD;
        return new MultiCompetitorRaceChartSettings(chartSettings, defaultDetailType, null);
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
    public Serializable getComponentId() {
        return getLocalizedShortName();
    }

    @Override
    public boolean hasSettings() {
        return true;
    }
}

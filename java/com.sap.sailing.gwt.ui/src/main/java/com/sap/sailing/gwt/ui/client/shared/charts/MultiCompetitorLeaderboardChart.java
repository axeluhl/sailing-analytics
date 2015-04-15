package com.sap.sailing.gwt.ui.client.shared.charts;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class MultiCompetitorLeaderboardChart extends AbstractCompetitorLeaderboardChart<MultiCompetitorLeaderboardChartSettings>  {
    private MultiCompetitorLeaderboardChartSettings settings;

    public MultiCompetitorLeaderboardChart(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, 
            String leaderboardName, DetailType detailType, CompetitorSelectionProvider competitorSelectionProvider,
            Timer timer, StringMessages stringMessages, ErrorReporter errorReporter) {
        super(sailingService, asyncActionsExecutor, leaderboardName, detailType, competitorSelectionProvider, timer, stringMessages, errorReporter);
        settings = new MultiCompetitorLeaderboardChartSettings(detailType);
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<MultiCompetitorLeaderboardChartSettings> getSettingsDialogComponent() {
        MultiCompetitorLeaderboardChartSettings chartSettings = new MultiCompetitorLeaderboardChartSettings(settings.getDetailType());
        return new MultiCompetitorLeaderboardChartSettingsDialogComponent(chartSettings, stringMessages);
    }

    @Override
    public void updateSettings(MultiCompetitorLeaderboardChartSettings newSettings) {
        boolean redraw = false;
        
        if(settings.getDetailType() != newSettings.getDetailType()) {
            settings = new MultiCompetitorLeaderboardChartSettings(newSettings.getDetailType());
            setSelectedDetailType(newSettings.getDetailType());
            redraw = true;
        }
        
        if(redraw) {
            clearChart();
            timeChanged(timer.getTime(), null);
        }
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.showCharts();
    }
}

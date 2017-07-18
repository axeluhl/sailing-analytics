package com.sap.sailing.gwt.ui.client.shared.charts;

import com.google.gwt.user.client.Window;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.settings.client.leaderboard.MultiCompetitorLeaderboardChartLifecycle;
import com.sap.sailing.gwt.settings.client.leaderboard.MultiCompetitorLeaderboardChartSettings;
import com.sap.sailing.gwt.settings.client.leaderboard.MultiCompetitorLeaderboardChartSettingsDialogComponent;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class MultiCompetitorLeaderboardChart extends AbstractCompetitorLeaderboardChart<MultiCompetitorLeaderboardChartSettings>  {
    private MultiCompetitorLeaderboardChartSettings settings;
    private final boolean isOverall;

    public MultiCompetitorLeaderboardChart(Component<?> parent, ComponentContext<?> context,
            SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, 
            String leaderboardName, DetailType detailType, CompetitorSelectionProvider competitorSelectionProvider,
            Timer timer, StringMessages stringMessages, boolean isOverall, ErrorReporter errorReporter) {
        super(parent, context,sailingService, asyncActionsExecutor, leaderboardName, detailType, competitorSelectionProvider,
                timer, stringMessages, errorReporter);
        this.isOverall = isOverall;
        settings = MultiCompetitorLeaderboardChartSettings.createWithDefaultDetailType(isOverall, detailType);
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public MultiCompetitorLeaderboardChartSettings getSettings() {
        return MultiCompetitorLeaderboardChartSettings
                .createWithDefaultDetailType(isOverall, settings.getDetailType());
    }
    
    @Override
    public SettingsDialogComponent<MultiCompetitorLeaderboardChartSettings> getSettingsDialogComponent(MultiCompetitorLeaderboardChartSettings settings) {
        return new MultiCompetitorLeaderboardChartSettingsDialogComponent(settings, isOverall);
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
    
    public void forceMaximumChartSize() {
        chart.setWidth100();
        chart.setHeight100();
        chart.redraw();
    }
    
    public void forceChartToClientHeight() {
        chart.setSize(chart.getOffsetWidth(), Window.getClientHeight());
        chart.redraw();
    }

    @Override
    public String getId() {
        return MultiCompetitorLeaderboardChartLifecycle.ID;
    }
}

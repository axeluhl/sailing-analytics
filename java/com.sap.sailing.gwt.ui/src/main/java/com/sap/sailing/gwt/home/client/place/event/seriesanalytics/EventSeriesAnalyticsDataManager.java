package com.sap.sailing.gwt.home.client.place.event.seriesanalytics;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.DebugIdHelper;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorLeaderboardChart;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorLeaderboardChartSettings;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sailing.gwt.ui.leaderboard.MultiLeaderboardPanel;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

/**
 * A viewer for an overall series leaderboard. Additionally the viewer can render a chart for the series leaderboard and
 * a MultiLeaderboardPanel where the user can select to show one leaderboard of the series.
 * 
 * @author Frank Mittag (c163874)
 * @author Axel Uhl (d043530)
 */
public class EventSeriesAnalyticsDataManager {
    private LeaderboardPanel overallLeaderboardPanel;
    private MultiCompetitorLeaderboardChart multiCompetitorChart;
    private MultiLeaderboardPanel multiLeaderboardPanel;

    private final CompetitorSelectionModel competitorSelectionProvider;
    private final AsyncActionsExecutor asyncActionsExecutor;
    private final ErrorReporter errorReporter;
    private final UserAgentDetails userAgent;
    private final SailingServiceAsync sailingService;
    private final Timer timer;

    public EventSeriesAnalyticsDataManager(final SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, Timer timer, ErrorReporter errorReporter, UserAgentDetails userAgent) {
        this.competitorSelectionProvider = new CompetitorSelectionModel(/* hasMultiSelection */true);
        this.sailingService = sailingService;
        this.asyncActionsExecutor = asyncActionsExecutor;
        this.timer = timer;
        this.errorReporter = errorReporter;
        this.userAgent = userAgent;
        this.overallLeaderboardPanel = null;
        this.multiCompetitorChart = null;
    }

    public LeaderboardPanel createOverallLeaderboardPanel(final LeaderboardSettings leaderboardSettings, final RaceIdentifier preselectedRace,
            final String leaderboardGroupName, String leaderboardName, boolean showRaceDetails, 
            boolean autoExpandLastRaceColumn) {
        if(overallLeaderboardPanel == null) {
            overallLeaderboardPanel = new LeaderboardPanel(sailingService, asyncActionsExecutor, leaderboardSettings, true, preselectedRace,
                    competitorSelectionProvider, timer, leaderboardGroupName, leaderboardName, errorReporter,
                    StringMessages.INSTANCE, userAgent, showRaceDetails, /* competitorSearchTextBox */ null, /* showSelectionCheckbox */ true, /* raceTimesInfoProvider */null, autoExpandLastRaceColumn, /* adjustTimerDelay */
                    true, false, false);
        }
        return overallLeaderboardPanel;
    }

    public MultiCompetitorLeaderboardChart createMultiCompetitorChart(String leaderboardName, DetailType chartDetailType) {
        if(multiCompetitorChart == null) {
            multiCompetitorChart = new MultiCompetitorLeaderboardChart(sailingService, asyncActionsExecutor, leaderboardName, chartDetailType,
                    competitorSelectionProvider, timer, StringMessages.INSTANCE, errorReporter);
            multiCompetitorChart.setVisible(false); 
        }
        return multiCompetitorChart;
    }

    public MultiLeaderboardPanel createMultiLeaderboardPanel(LeaderboardSettings leaderboardSettings,
            String preselectedLeaderboardName, RaceIdentifier preselectedRace, String leaderboardGroupName,
            String metaLeaderboardName, boolean showRaceDetails, boolean autoExpandLastRaceColumn) {
        if(multiLeaderboardPanel == null) {
            multiLeaderboardPanel = new MultiLeaderboardPanel(sailingService, metaLeaderboardName, asyncActionsExecutor, timer, true /*isEmbedded*/,
                    preselectedLeaderboardName, preselectedRace, errorReporter, StringMessages.INSTANCE,
                    userAgent, showRaceDetails, autoExpandLastRaceColumn);
        }
        return multiLeaderboardPanel;
    }

    public MultiLeaderboardPanel getMultiLeaderboardPanel() {
        return multiLeaderboardPanel;
    }

    public LeaderboardPanel getLeaderboardPanel() {
        return overallLeaderboardPanel;
    }

    public MultiCompetitorLeaderboardChart getMultiCompetitorChart() {
        return multiCompetitorChart;
    }

    public void showCompetitorChart(DetailType chartDetailType) {
        MultiCompetitorLeaderboardChart multiCompetitorChart = getMultiCompetitorChart();
        MultiCompetitorLeaderboardChartSettings settings = new MultiCompetitorLeaderboardChartSettings(chartDetailType);
        multiCompetitorChart.updateSettings(settings);
        multiCompetitorChart.setVisible(true);
        timer.addTimeListener(multiCompetitorChart);
        multiCompetitorChart.timeChanged(timer.getTime(), null);
    }

    public void hideCompetitorChart() {
        MultiCompetitorLeaderboardChart multiCompetitorChart = getMultiCompetitorChart();
        multiCompetitorChart.setVisible(false);
        timer.removeTimeListener(multiCompetitorChart);
    }
    
    public void showOverallLeaderboardSettingsDialog() {
        showComponentSettingsDialog(overallLeaderboardPanel, null);
    }

    public void showRegattaLeaderboardsSettingsDialog() {
        showComponentSettingsDialog(multiLeaderboardPanel, null);
    }

    public void showChartSettingsDialog() {
        showComponentSettingsDialog(multiCompetitorChart, null);
    }
    
    protected <SettingsType> void showComponentSettingsDialog(final Component<SettingsType> component, String componentDisplayName) {
        String componentName = componentDisplayName != null ? componentDisplayName : component.getLocalizedShortName();
        String debugIdPrefix = DebugIdHelper.createDebugId(componentName);
        SettingsDialog<SettingsType> dialog = new SettingsDialog<SettingsType>(component, StringMessages.INSTANCE);
        dialog.ensureDebugId(debugIdPrefix + "SettingsDialog");
        dialog.show();
    }
}

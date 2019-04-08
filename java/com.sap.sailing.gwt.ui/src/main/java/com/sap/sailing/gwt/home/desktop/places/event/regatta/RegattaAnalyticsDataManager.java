package com.sap.sailing.gwt.home.desktop.places.event.regatta;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.gwt.settings.client.leaderboard.MultiCompetitorLeaderboardChartSettings;
import com.sap.sailing.gwt.settings.client.leaderboard.MultiRaceLeaderboardSettings;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.DebugIdHelper;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.SailingClientFactory;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorLeaderboardChart;
import com.sap.sailing.gwt.ui.leaderboard.ClassicLeaderboardStyle;
import com.sap.sailing.gwt.ui.leaderboard.MultiRaceLeaderboardPanel;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

/**
 * A class managing analytical data on the regatta level (like leaderboard, regatta rank, etc.)
 * 
 * @author Frank Mittag (c163874)
 */
public class RegattaAnalyticsDataManager {
    private MultiRaceLeaderboardPanel leaderboardPanel;
    private MultiCompetitorLeaderboardChart multiCompetitorChart;

    private final CompetitorSelectionModel competitorSelectionProvider;
    private final AsyncActionsExecutor asyncActionsExecutor;
    private final ErrorReporter errorReporter;
    private final SailingClientFactory sailingCF;
    private final Timer timer;
    private final FlagImageResolver flagImageResolver;
    
    public RegattaAnalyticsDataManager(final SailingClientFactory sailingCF,
            AsyncActionsExecutor asyncActionsExecutor, Timer timer, ErrorReporter errorReporter,
            FlagImageResolver flagImageResolver) {
        this.flagImageResolver = flagImageResolver;
        this.competitorSelectionProvider = new CompetitorSelectionModel(/* hasMultiSelection */true);
        this.sailingCF = sailingCF;
        this.asyncActionsExecutor = asyncActionsExecutor;
        this.timer = timer;
        this.errorReporter = errorReporter;
        this.leaderboardPanel = null;
        this.multiCompetitorChart = null;
    }

    public MultiRaceLeaderboardPanel createMultiRaceLeaderboardPanel(Component<?> parent, ComponentContext<?> context,
            final MultiRaceLeaderboardSettings leaderboardSettings,
            final String leaderboardGroupName, String leaderboardName, boolean showRaceDetails, 
            boolean autoExpandLastRaceColumn, Iterable<DetailType> availableDetailTypes) {
        if (leaderboardPanel == null) {
            SailingServiceAsync sailingService = sailingCF.getSailingService(()-> leaderboardName);
            leaderboardPanel = new MultiRaceLeaderboardPanel(parent, context, sailingService,
                    asyncActionsExecutor,
                    leaderboardSettings,
                    true, 
                    competitorSelectionProvider, timer, leaderboardGroupName, leaderboardName, errorReporter,
                    StringMessages.INSTANCE, showRaceDetails, /* competitorSearchTextBox */ null,
                    /* showSelectionCheckbox */ true, /* raceTimesInfoProvider */ null, autoExpandLastRaceColumn,
                    /* adjustTimerDelay */ true, /* autoApplyTopNFilter */ false, /* showCompetitorFilterStatus */ false, /* enableSyncScroller */ true,
                    new ClassicLeaderboardStyle(), flagImageResolver, availableDetailTypes);
        }
        return leaderboardPanel;
    }

    public MultiCompetitorLeaderboardChart createMultiCompetitorChart(String leaderboardName, DetailType chartDetailType) {
        if(multiCompetitorChart == null) {
            SailingServiceAsync sailingService = sailingCF.getSailingService(()-> leaderboardName);
            multiCompetitorChart = new MultiCompetitorLeaderboardChart(null, null, sailingService, asyncActionsExecutor,
                    leaderboardName, chartDetailType,
                    competitorSelectionProvider, timer, StringMessages.INSTANCE, false, errorReporter);
            multiCompetitorChart.setVisible(false); 
        }
        return multiCompetitorChart;
    }
    
    public MultiRaceLeaderboardPanel getLeaderboardPanel() {
        return leaderboardPanel;
    }

    public MultiCompetitorLeaderboardChart getMultiCompetitorChart() {
        return multiCompetitorChart;
    }

    public CompetitorSelectionModel getCompetitorSelectionProvider() {
        return competitorSelectionProvider;
    }

    public void showCompetitorChart(DetailType chartDetailType) {
        MultiCompetitorLeaderboardChart multiCompetitorChart = getMultiCompetitorChart();
        MultiCompetitorLeaderboardChartSettings settings = new MultiCompetitorLeaderboardChartSettings(chartDetailType);
        multiCompetitorChart.updateSettings(settings);
        multiCompetitorChart.setVisible(true);
        timer.addTimeListener(multiCompetitorChart);
        multiCompetitorChart.clearChart();
        multiCompetitorChart.timeChanged(timer.getTime(), null);
    }

    public void hideCompetitorChart() {
        MultiCompetitorLeaderboardChart multiCompetitorChart = getMultiCompetitorChart();
        if (multiCompetitorChart != null) {
            multiCompetitorChart.setVisible(false);
            timer.removeTimeListener(multiCompetitorChart);
        }
    }

    public void showLeaderboardSettingsDialog() {
        showComponentSettingsDialog(leaderboardPanel, null);
    }
    
    public void showChartSettingsDialog() {
        showComponentSettingsDialog(multiCompetitorChart, null);
    }
    
    protected <SettingsType extends Settings> void showComponentSettingsDialog(final Component<SettingsType> component, String componentDisplayName) {
        String componentName = componentDisplayName != null ? componentDisplayName : component.getLocalizedShortName();
        String debugIdPrefix = DebugIdHelper.createDebugId(componentName);
        SettingsDialog<SettingsType> dialog = new SettingsDialog<SettingsType>(component, StringMessages.INSTANCE);
        dialog.ensureDebugId(debugIdPrefix + "SettingsDialog");
        dialog.show();
    }
    
    public Timer getTimer() {
        return timer;
    }
}

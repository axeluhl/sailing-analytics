package com.sap.sailing.gwt.home.desktop.places.fakeseries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.settings.client.leaderboard.MultiCompetitorLeaderboardChartSettings;
import com.sap.sailing.gwt.settings.client.leaderboard.MultiRaceLeaderboardSettings;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.DebugIdHelper;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sailing.gwt.ui.client.SailingClientFactory;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorLeaderboardChart;
import com.sap.sailing.gwt.ui.leaderboard.ClassicLeaderboardStyle;
import com.sap.sailing.gwt.ui.leaderboard.MultiLeaderboardProxyPanel;
import com.sap.sailing.gwt.ui.leaderboard.MultiRaceLeaderboardPanel;
import com.sap.sse.common.Util;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

/**
 * A viewer for an overall series leaderboard. Additionally the viewer can render a chart for the series leaderboard and
 * a MultiLeaderboardPanel where the user can select to show one leaderboard of the series.
 * 
 * @author Frank Mittag (c163874)
 * @author Axel Uhl (d043530)
 */
public class EventSeriesAnalyticsDataManager {
    private MultiRaceLeaderboardPanel overallLeaderboardPanel;
    private MultiCompetitorLeaderboardChart multiCompetitorChart;
    private MultiLeaderboardProxyPanel multiLeaderboardPanel;

    private final CompetitorSelectionModel competitorSelectionProvider;
    private final AsyncActionsExecutor asyncActionsExecutor;
    private final ErrorReporter errorReporter;
    private final SailingClientFactory sailingCF;
    private final Timer timer;
    private final int MAX_COMPETITORS_IN_CHART = 30;
    private final FlagImageResolver flagImageResolver; 

    public EventSeriesAnalyticsDataManager(final SailingClientFactory sailingCF,
            AsyncActionsExecutor asyncActionsExecutor, Timer timer, ErrorReporter errorReporter,
            FlagImageResolver flagImageResolver) {
        this.flagImageResolver = flagImageResolver;
        this.competitorSelectionProvider = new CompetitorSelectionModel(/* hasMultiSelection */true);
        this.sailingCF = sailingCF;
        this.asyncActionsExecutor = asyncActionsExecutor;
        this.timer = timer;
        this.errorReporter = errorReporter;
        this.overallLeaderboardPanel = null;
        this.multiCompetitorChart = null;
    }

    public MultiRaceLeaderboardPanel createMultiRaceOverallLeaderboardPanel(Component<?> parent, ComponentContext<?> context,
            final MultiRaceLeaderboardSettings leaderboardSettings,
            final String leaderboardGroupName, String leaderboardName, boolean showRaceDetails, 
            boolean autoExpandLastRaceColumn, Iterable<DetailType> availableDetailTypes) {
        
        
        if(overallLeaderboardPanel == null) {
            SailingServiceAsync sailingService = sailingCF.getSailingService(()-> leaderboardName);
            overallLeaderboardPanel = new MultiRaceLeaderboardPanel(parent, context, sailingService, asyncActionsExecutor,
                    leaderboardSettings, true, 
                    competitorSelectionProvider, timer, leaderboardGroupName, leaderboardName, errorReporter,
                    StringMessages.INSTANCE, showRaceDetails, /* competitorSearchTextBox */ null,
                    /* showSelectionCheckbox */ true,
                    /* raceTimesInfoProvider */null, autoExpandLastRaceColumn, /* adjustTimerDelay */ true, /* autoApplyTopNFilter */ false,
                    /* showCompetitorFilterStatus */ false, /* enableSyncScroller */ false, new ClassicLeaderboardStyle(),
                    flagImageResolver, availableDetailTypes);
        }
        return overallLeaderboardPanel;
    }

    public MultiCompetitorLeaderboardChart createMultiCompetitorChart(Component<?> parent,
            ComponentContext<?> context, String leaderboardName,
            DetailType chartDetailType) {
        
        if(multiCompetitorChart == null) {
            SailingServiceAsync sailingService = sailingCF.getSailingService(()-> leaderboardName);

            multiCompetitorChart = new MultiCompetitorLeaderboardChart(parent, context, sailingService,
                    asyncActionsExecutor,
                    leaderboardName, chartDetailType,
                    competitorSelectionProvider, timer, StringMessages.INSTANCE, true, errorReporter);
            multiCompetitorChart.setVisible(false); 
        }
        return multiCompetitorChart;
    }

    public MultiLeaderboardProxyPanel createMultiLeaderboardPanel(Component<?> parent, ComponentContext<?> context,
            MultiRaceLeaderboardSettings leaderboardSettings,
            String preselectedLeaderboardName,  String leaderboardGroupName,
            String metaLeaderboardName, boolean showRaceDetails, boolean autoExpandLastRaceColumn, Iterable<DetailType> availableDetailTypes) {
        if (multiLeaderboardPanel == null) {
            final Function<String, SailingServiceAsync> sailingServiceFactory = leaderBoardName -> sailingCF
                    .getSailingService(() -> leaderBoardName);
            multiLeaderboardPanel = new MultiLeaderboardProxyPanel(parent, context, sailingServiceFactory, metaLeaderboardName,
                    asyncActionsExecutor, timer, true /* isEmbedded */,
                    preselectedLeaderboardName, errorReporter, StringMessages.INSTANCE,
                    showRaceDetails, autoExpandLastRaceColumn, leaderboardSettings, flagImageResolver, availableDetailTypes);
        }
        return multiLeaderboardPanel;
    }

    public MultiLeaderboardProxyPanel getMultiLeaderboardPanel() {
        return multiLeaderboardPanel;
    }

    public MultiRaceLeaderboardPanel getLeaderboardPanel() {
        return overallLeaderboardPanel;
    }

    public MultiCompetitorLeaderboardChart getMultiCompetitorChart() {
        return multiCompetitorChart;
    }

    public void showCompetitorChart(DetailType chartDetailType) {
        // preselect the top N competitors in case there was no selection before and there too many competitors for a chart
        int competitorsCount = Util.size(competitorSelectionProvider.getAllCompetitors());
        int selectedCompetitorsCount = Util.size(competitorSelectionProvider.getSelectedCompetitors());
        
        if(selectedCompetitorsCount == 0 && competitorsCount > MAX_COMPETITORS_IN_CHART) {
            List<CompetitorDTO> selectedCompetitors = new ArrayList<>();
            Iterator<CompetitorDTO> allCompetitorsIt = competitorSelectionProvider.getAllCompetitors().iterator();
            int counter = 0;
            while(counter < MAX_COMPETITORS_IN_CHART) {
                selectedCompetitors.add(allCompetitorsIt.next());
                counter++;
            }
            competitorSelectionProvider.setSelection(selectedCompetitors, (CompetitorSelectionChangeListener[]) null);
        }
        
        MultiCompetitorLeaderboardChart multiCompetitorChart = getMultiCompetitorChart();
        MultiCompetitorLeaderboardChartSettings settings = new MultiCompetitorLeaderboardChartSettings(chartDetailType);
        multiCompetitorChart.updateSettings(settings);
        multiCompetitorChart.setVisible(true);
        timer.addTimeListener(multiCompetitorChart);
        multiCompetitorChart.clearChart();
        multiCompetitorChart.timeChanged(timer.getTime(), null);
    }

    public CompetitorSelectionModel getCompetitorSelectionProvider() {
        return competitorSelectionProvider;
    }

    public void hideCompetitorChart() {
        MultiCompetitorLeaderboardChart multiCompetitorChart = getMultiCompetitorChart();
        if (multiCompetitorChart != null) {
            multiCompetitorChart.setVisible(false);
            timer.removeTimeListener(multiCompetitorChart);
        }
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

    public void getAvailableDetailTypesForLeaderboard(String leaderboardName, RegattaAndRaceIdentifier raceOrNull,
            AsyncCallback<Iterable<DetailType>> asyncCallback) {
        SailingServiceAsync sailingService = sailingCF.getSailingService(()-> leaderboardName);
        sailingService.getAvailableDetailTypesForLeaderboard(leaderboardName, raceOrNull, asyncCallback);
    }
}

package com.sap.sailing.gwt.home.client.place.event2.regatta;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionChangeListener;
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
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

/**
 * A class managing analytical data on the regatta level (like leaderboard, regatta rank, etc.)
 * 
 * @author Frank Mittag (c163874)
 */
public class RegattaAnalyticsDataManager {
    private LeaderboardPanel leaderboardPanel;
    private MultiCompetitorLeaderboardChart multiCompetitorChart;

    private final CompetitorSelectionModel competitorSelectionProvider;
    private final AsyncActionsExecutor asyncActionsExecutor;
    private final ErrorReporter errorReporter;
    private final UserAgentDetails userAgent;
    private final SailingServiceAsync sailingService;
    private final Timer timer;
    private final int MAX_COMPETITORS_IN_CHART = 30; 
    
    public RegattaAnalyticsDataManager(final SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, Timer timer, ErrorReporter errorReporter, UserAgentDetails userAgent) {
        this.competitorSelectionProvider = new CompetitorSelectionModel(/* hasMultiSelection */true);
        this.sailingService = sailingService;
        this.asyncActionsExecutor = asyncActionsExecutor;
        this.timer = timer;
        this.errorReporter = errorReporter;
        this.userAgent = userAgent;
        this.leaderboardPanel = null;
        this.multiCompetitorChart = null;
    }

    public LeaderboardPanel createLeaderboardPanel(final LeaderboardSettings leaderboardSettings, final RaceIdentifier preselectedRace,
            final String leaderboardGroupName, String leaderboardName, boolean showRaceDetails, 
            boolean autoExpandLastRaceColumn) {
        if(leaderboardPanel == null) {
            leaderboardPanel = new LeaderboardPanel(sailingService, asyncActionsExecutor, leaderboardSettings, true, preselectedRace,
                    competitorSelectionProvider, timer, leaderboardGroupName, leaderboardName, errorReporter,
                    StringMessages.INSTANCE, userAgent, showRaceDetails, /* competitorSearchTextBox */ null, /* showSelectionCheckbox */ true, /* raceTimesInfoProvider */null, autoExpandLastRaceColumn, /* adjustTimerDelay */
                    true, false, false);
        }
        return leaderboardPanel;
    }

    public MultiCompetitorLeaderboardChart createMultiCompetitorChart(String leaderboardName, DetailType chartDetailType) {
        if(multiCompetitorChart == null) {
            multiCompetitorChart = new MultiCompetitorLeaderboardChart(sailingService, asyncActionsExecutor, leaderboardName, chartDetailType,
                    competitorSelectionProvider, timer, StringMessages.INSTANCE, errorReporter);
            multiCompetitorChart.setVisible(false); 
        }
        return multiCompetitorChart;
    }
    
    public LeaderboardPanel getLeaderboardPanel() {
        return leaderboardPanel;
    }

    public MultiCompetitorLeaderboardChart getMultiCompetitorChart() {
        return multiCompetitorChart;
    }

    public CompetitorSelectionModel getCompetitorSelectionProvider() {
        return competitorSelectionProvider;
    }

    public void showCompetitorChart(DetailType chartDetailType) {
        // preselect the top N competitors in case there was no selection before and there too many competitors for a chart
        int competitorsCount = Util.size(competitorSelectionProvider.getAllCompetitors());
        int selectedCompetitorsCount = Util.size(competitorSelectionProvider.getSelectedCompetitors());
        
        if(selectedCompetitorsCount == 0 && competitorsCount > MAX_COMPETITORS_IN_CHART) {
            List<CompetitorDTO> selectedCompetitors = new ArrayList<CompetitorDTO>();
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
    
    protected <SettingsType> void showComponentSettingsDialog(final Component<SettingsType> component, String componentDisplayName) {
        String componentName = componentDisplayName != null ? componentDisplayName : component.getLocalizedShortName();
        String debugIdPrefix = DebugIdHelper.createDebugId(componentName);
        SettingsDialog<SettingsType> dialog = new SettingsDialog<SettingsType>(component, StringMessages.INSTANCE);
        dialog.ensureDebugId(debugIdPrefix + "SettingsDialog");
        dialog.show();
    }
}

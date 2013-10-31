package com.sap.sailing.gwt.ui.leaderboard;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.client.UserAgentDetails;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorLeaderboardChart;

/**
 * A viewer for a single leaderboard and a leaderboard chart.
 * @author Frank Mittag (c163874)
 *
 */
public class LeaderboardViewer extends AbstractLeaderboardViewer {

    private final LeaderboardPanel leaderboardPanel;
    private final MultiCompetitorLeaderboardChart multiCompetitorChart;
    
    public LeaderboardViewer(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            LeaderboardSettings leaderboardSettings, RaceIdentifier preselectedRace, String leaderboardGroupName,
            String leaderboardName, ErrorReporter errorReporter, StringMessages stringMessages,
            UserAgentDetails userAgent, boolean showRaceDetails, boolean hideToolbar, boolean autoExpandLastRaceColumn, 
            boolean showCharts, DetailType chartDetailType, boolean showOverallLeaderboard) {
        super(new CompetitorSelectionModel(/* hasMultiSelection */true), asyncActionsExecutor, 
                new Timer(PlayModes.Replay, /*delayBetweenAutoAdvancesInMilliseconds*/ 3000l), stringMessages, hideToolbar);

        FlowPanel mainPanel = createViewerPanel();
        setWidget(mainPanel);

        leaderboardPanel = new LeaderboardPanel(sailingService, asyncActionsExecutor,
                leaderboardSettings, preselectedRace, competitorSelectionProvider, timer,
                leaderboardGroupName, leaderboardName, errorReporter, stringMessages, userAgent,
                showRaceDetails, /* raceTimesInfoProvider */null, autoExpandLastRaceColumn,  /* adjustTimerDelay */ true);

        multiCompetitorChart = new MultiCompetitorLeaderboardChart(sailingService, asyncActionsExecutor, leaderboardName, chartDetailType,
                competitorSelectionProvider, timer, stringMessages, errorReporter);
        multiCompetitorChart.setVisible(showCharts); 
        multiCompetitorChart.getElement().getStyle().setMarginTop(10, Unit.PX);
        multiCompetitorChart.getElement().getStyle().setMarginBottom(10, Unit.PX);

        mainPanel.add(leaderboardPanel);
        mainPanel.add(multiCompetitorChart);

        addComponentToNavigationMenu(leaderboardPanel, false, null, /* hasSettingsWhenComponentIsInvisible*/ true);
        addComponentToNavigationMenu(multiCompetitorChart, true, null,  /* hasSettingsWhenComponentIsInvisible*/ true);
        
        if(showCharts) {
            multiCompetitorChart.timeChanged(timer.getTime());
        }
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        
        if(visible) {
            timer.addTimeListener(multiCompetitorChart);
        } else {
            timer.removeTimeListener(multiCompetitorChart);
        }
    }
}

package com.sap.sailing.gwt.ui.leaderboard;

import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorLeaderboardChart;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

/**
 * A viewer for a single leaderboard and a leaderboard chart.
 * @author Frank Mittag (c163874)
 *
 */
public class LeaderboardViewer extends AbstractLeaderboardViewer {

    private final LeaderboardPanel leaderboardPanel;
    private final MultiCompetitorLeaderboardChart multiCompetitorChart;
    private LeaderboardPanel overallLeaderboardPanel;
    
    public LeaderboardViewer(final SailingServiceAsync sailingService, final AsyncActionsExecutor asyncActionsExecutor,
            final LeaderboardSettings leaderboardSettings, final RaceIdentifier preselectedRace, final String leaderboardGroupName,
            String leaderboardName, final ErrorReporter errorReporter, final StringMessages stringMessages,
            final UserAgentDetails userAgent, boolean showRaceDetails, boolean hideToolbar, boolean autoExpandLastRaceColumn, 
            boolean showCharts, DetailType chartDetailType, boolean showOverallLeaderboard) {
        super(new CompetitorSelectionModel(/* hasMultiSelection */true), asyncActionsExecutor, 
                new Timer(PlayModes.Replay, /*delayBetweenAutoAdvancesInMilliseconds*/ 3000l), stringMessages, hideToolbar);

        final FlowPanel mainPanel = createViewerPanel();
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
        // Remark: For now we only add the overallLeaderboardPanel to the navigation menu in case it's visible from the beginning
        
        if(showCharts) {
            multiCompetitorChart.timeChanged(timer.getTime(), null);
        }
        
        overallLeaderboardPanel = null;
        if(showOverallLeaderboard) {
            sailingService.getOverallLeaderboardNamesContaining(leaderboardName, new MarkedAsyncCallback<List<String>>(
                    new AsyncCallback<List<String>>() {
                        @Override
                        public void onSuccess(List<String> result) {
                            if(result.size() == 1) {
                                String overallLeaderboardName = result.get(0);
                                overallLeaderboardPanel = new LeaderboardPanel(sailingService, asyncActionsExecutor,
                                        leaderboardSettings, preselectedRace, competitorSelectionProvider, timer,
                                        leaderboardGroupName, overallLeaderboardName, errorReporter, stringMessages, userAgent,
                                        false, /* raceTimesInfoProvider */null, false,  /* adjustTimerDelay */ true);
                                mainPanel.add(overallLeaderboardPanel);
                                addComponentToNavigationMenu(overallLeaderboardPanel, true, stringMessages.seriesLeaderboard(),
                                        /* hasSettingsWhenComponentIsInvisible*/ true);
                            }
                        }
                        
                        @Override
                        public void onFailure(Throwable caught) {
                            // DO NOTHING
                        }
            }));
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

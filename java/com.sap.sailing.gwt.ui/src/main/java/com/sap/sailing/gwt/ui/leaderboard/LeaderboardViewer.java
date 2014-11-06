package com.sap.sailing.gwt.ui.leaderboard;

import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.MultiCompetitorLeaderboardChart;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

/**
 * A viewer for a single leaderboard and a leaderboard chart.
 * @author Frank Mittag (c163874)
 *
 */
public class LeaderboardViewer extends AbstractLeaderboardViewer {
    private final MultiCompetitorLeaderboardChart multiCompetitorChart;
    private LeaderboardPanel overallLeaderboardPanel;
    
    public LeaderboardViewer(final SailingServiceAsync sailingService, final AsyncActionsExecutor asyncActionsExecutor,
            final Timer timer, final LeaderboardSettings leaderboardSettings, final RaceIdentifier preselectedRace,
            final String leaderboardGroupName, String leaderboardName, final ErrorReporter errorReporter,
            final StringMessages stringMessages, final UserAgentDetails userAgent, boolean showRaceDetails, boolean hideToolbar, 
            boolean autoExpandLastRaceColumn, boolean showCharts, DetailType chartDetailType, boolean showOverallLeaderboard) {
        this(new CompetitorSelectionModel(/* hasMultiSelection */true), sailingService, asyncActionsExecutor, timer,
                leaderboardSettings, preselectedRace, leaderboardGroupName, leaderboardName, errorReporter,
                stringMessages, userAgent, showRaceDetails, hideToolbar, autoExpandLastRaceColumn, showCharts,
                chartDetailType, showOverallLeaderboard);
    }

    private LeaderboardViewer(CompetitorSelectionModel competitorSelectionModel,
            final SailingServiceAsync sailingService, final AsyncActionsExecutor asyncActionsExecutor,
            final Timer timer, final LeaderboardSettings leaderboardSettings, final RaceIdentifier preselectedRace,
            final String leaderboardGroupName, String leaderboardName, final ErrorReporter errorReporter,
            final StringMessages stringMessages, final UserAgentDetails userAgent, boolean showRaceDetails,
            boolean hideToolbar, boolean autoExpandLastRaceColumn, boolean showCharts, DetailType chartDetailType,
            boolean showOverallLeaderboard) {
        super(competitorSelectionModel, asyncActionsExecutor, timer, stringMessages, hideToolbar, new LeaderboardPanel(
                sailingService, asyncActionsExecutor, leaderboardSettings, preselectedRace != null, preselectedRace,
                competitorSelectionModel, timer, leaderboardGroupName, leaderboardName, errorReporter,
                stringMessages, userAgent, showRaceDetails, /* competitorSearchTextBox */ null, /* showSelectionCheckbox */ true, /* adjustTimerDelay */
                /* raceTimesInfoProvider */null, autoExpandLastRaceColumn, true, /*autoApplyTopNFilter*/ false, false));
        final FlowPanel mainPanel = createViewerPanel();
        setWidget(mainPanel);
        multiCompetitorChart = new MultiCompetitorLeaderboardChart(sailingService, asyncActionsExecutor, leaderboardName, chartDetailType,
                competitorSelectionProvider, timer, stringMessages, errorReporter);
        multiCompetitorChart.setVisible(showCharts); 
        multiCompetitorChart.getElement().getStyle().setMarginTop(10, Unit.PX);
        multiCompetitorChart.getElement().getStyle().setMarginBottom(10, Unit.PX);

        mainPanel.add(getLeaderboardPanel());
        mainPanel.add(multiCompetitorChart);

        addComponentToNavigationMenu(getLeaderboardPanel(), false, null, /* hasSettingsWhenComponentIsInvisible*/ true);
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
                                        leaderboardSettings, preselectedRace != null, preselectedRace, competitorSelectionProvider, timer,
                                        leaderboardGroupName, overallLeaderboardName, errorReporter, stringMessages, userAgent,
                                        false, /* competitorSearchTextBox */ null, /* showSelectionCheckbox */ true,  /* raceTimesInfoProvider */null,
                                        false, /* adjustTimerDelay */ true, /*autoApplyTopNFilter*/ false, false);
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

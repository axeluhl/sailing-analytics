package com.sap.sailing.gwt.home.client.shared.leaderboard;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionModel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardPanel;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
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
    private LeaderboardPanel overallLeaderboardPanel;
    
    public LeaderboardViewer(final SailingServiceAsync sailingService, final AsyncActionsExecutor asyncActionsExecutor,
            final Timer timer, final LeaderboardSettings leaderboardSettings, final RaceIdentifier preselectedRace,
            final String leaderboardGroupName, String leaderboardName, final ErrorReporter errorReporter,
            final StringMessages stringMessages, final UserAgentDetails userAgent, boolean showRaceDetails, 
            boolean autoExpandLastRaceColumn, boolean showOverallLeaderboard) {
        this(new CompetitorSelectionModel(/* hasMultiSelection */true), sailingService, asyncActionsExecutor, timer,
                leaderboardSettings, preselectedRace, leaderboardGroupName, leaderboardName, errorReporter,
                stringMessages, userAgent, showRaceDetails, autoExpandLastRaceColumn, showOverallLeaderboard);
    }

    private LeaderboardViewer(CompetitorSelectionModel competitorSelectionModel,
            final SailingServiceAsync sailingService, final AsyncActionsExecutor asyncActionsExecutor,
            final Timer timer, final LeaderboardSettings leaderboardSettings, final RaceIdentifier preselectedRace,
            final String leaderboardGroupName, String leaderboardName, final ErrorReporter errorReporter,
            final StringMessages stringMessages, final UserAgentDetails userAgent, boolean showRaceDetails,
            boolean autoExpandLastRaceColumn, boolean showOverallLeaderboard) {
        super(competitorSelectionModel, asyncActionsExecutor, timer, stringMessages, new LeaderboardPanel(
                sailingService, asyncActionsExecutor, leaderboardSettings, preselectedRace,
                competitorSelectionModel, timer, leaderboardGroupName, leaderboardName, errorReporter,
                stringMessages, userAgent, showRaceDetails, /* raceTimesInfoProvider */null, /*showSelectionCheckbox*/false, null,
                autoExpandLastRaceColumn, /* adjustTimerDelay */true, false, false));
        final FlowPanel mainPanel = new FlowPanel();
        setWidget(mainPanel);

        mainPanel.add(getLeaderboardPanel());

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
                                        /*showRaceDetails*/false, null, /*showSelectionCheckbox*/true, null, /*autoExpandLastRaceColumn*/false,
                                        /*adjustTimerDelay*/true, /*autoApplyTopNFilter*/false, false);
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
}

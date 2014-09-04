package com.sap.sailing.gwt.home.client.shared.leaderboard;

import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

/**
 * A viewer for a single leaderboard, a ranks or total points chart.
 * @author Frank Mittag (c163874)
 *
 */
public class LeaderboardViewer extends AbstractRegattaAnalyticsManager {
//    private LeaderboardPanel overallLeaderboardPanel;
    
    public LeaderboardViewer(final SailingServiceAsync sailingService, final AsyncActionsExecutor asyncActionsExecutor,
            final Timer timer, final ErrorReporter errorReporter, final UserAgentDetails userAgent) {
        super(sailingService, asyncActionsExecutor, timer, errorReporter, userAgent);
        
//        overallLeaderboardPanel = null;
//        if(showOverallLeaderboard) {
//            sailingService.getOverallLeaderboardNamesContaining(leaderboardName, new MarkedAsyncCallback<List<String>>(
//                    new AsyncCallback<List<String>>() {
//                        @Override
//                        public void onSuccess(List<String> result) {
//                            if(result.size() == 1) {
//                                String overallLeaderboardName = result.get(0);
//                                overallLeaderboardPanel = new LeaderboardPanel(sailingService, asyncActionsExecutor,
//                                        leaderboardSettings, true, preselectedRace, competitorSelectionProvider, timer,
//                                        leaderboardGroupName, overallLeaderboardName, errorReporter, StringMessages.INSTANCE, userAgent,
//                                        false, /* competitorSearchTextBox */ null, /* showSelectionCheckbox */ true, /* raceTimesInfoProvider */null, false, 
//                                        /* adjustTimerDelay */ true, /*autoApplyTopNFilter*/false, false);
//                                mainPanel.add(overallLeaderboardPanel);
//                                addComponentToNavigationMenu(overallLeaderboardPanel, true, StringMessages.INSTANCE.seriesLeaderboard(),
//                                        /* hasSettingsWhenComponentIsInvisible*/ true);
//                            }
//                        }
//                        
//                        @Override
//                        public void onFailure(Throwable caught) {
//                            // DO NOTHING
//                        }
//            }));
//        }
    }
    
}

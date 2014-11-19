package com.sap.sailing.gwt.home.client.place.leaderboard;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardSettings;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.useragent.UserAgentDetails;

public interface AnalyticsView {
    Widget asWidget();
    
    void createRegattaAnalyticsViewer(final SailingServiceAsync sailingService, final AsyncActionsExecutor asyncActionsExecutor,
            final Timer timer, final LeaderboardSettings leaderboardSettings, final RegattaAndRaceIdentifier preselectedRace,
            final String leaderboardGroupName, String leaderboardName, final ErrorReporter errorReporter,
            final UserAgentDetails userAgent, boolean showRaceDetails,  
            boolean autoExpandLastRaceColumn,  boolean showOverallLeaderboard);

    void createSeriesAnalyticsViewer(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor, 
            Timer timer, LeaderboardSettings leaderboardSettings, String preselectedLeaderboardName, RegattaAndRaceIdentifier preselectedRace,
            String leaderboardGroupName, String metaLeaderboardName, ErrorReporter errorReporter,
            UserAgentDetails userAgent, boolean showRaceDetails,  
            boolean autoExpandLastRaceColumn, boolean showSeriesLeaderboards);
}
